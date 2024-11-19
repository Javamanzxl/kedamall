package com.zxl.gulimall.product.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxl.gulimall.product.service.CategoryBrandRelationService;
import com.zxl.gulimall.product.vo.Catalog2Vo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.Query;

import com.zxl.gulimall.product.dao.CategoryDao;
import com.zxl.gulimall.product.entity.CategoryEntity;
import com.zxl.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryDao categoryDao;
    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listTree() {
        List<CategoryEntity> categoryAllList = categoryDao.selectList(null);
        List<CategoryEntity> list = categoryAllList.stream().filter((category) -> category.getParentCid() == 0)
                .map((category) -> {
                    category.setChildren(getChildrens(category, categoryAllList));
                    return category;
                }).sorted(Comparator.comparingInt(category -> (category.getSort() == null ? 0 : category.getSort())))
                .toList();
        return list;
    }

    /**
     * 递归查找所有菜单的子菜单
     *
     * @param category
     * @param categoryAllList
     * @return
     */
    private List<CategoryEntity> getChildrens(CategoryEntity category, List<CategoryEntity> categoryAllList) {
        List<CategoryEntity> children = categoryAllList.stream().filter((entity) -> category.getCatId() == entity.getParentCid())
                .map((entity) -> {
                    entity.setChildren(getChildrens(entity, categoryAllList));
                    return entity;
                }).sorted(Comparator.comparingInt(entity -> (entity.getSort() == null ? 0 : entity.getSort()))).toList();
        return children;
    }

    @Override
    public void removeMenusByIdS(List<Long> list) {
        //TODO 1.检查当前删除的菜单，是否被其他地方引用
        categoryDao.deleteBatchIds(list);
    }

    /**
     * 找到catelogId的完整路径
     * [父\子\孙]
     *
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1.收集当前节点id
        paths.add(catelogId);
        CategoryEntity category = categoryDao.selectById(catelogId);
        if (category.getParentCid() != 0) {
            findParentPath(category.getParentCid(), paths);
        }
        return paths;
    }

    /**
     * 级联更新相关数据表
     * @CacheEvict:实现失效模式
     *
     * @param category
     */
    @CacheEvict(value = {"category"},key ="'level1Category1'")
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        categoryDao.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            //更新categoryBrandRelation表的数据
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
            redisTemplate.delete("catalogJsonFromDb");
            //TODO:更新其他级联表
        }
    }

    /**
     * 用户端，查询出所有一级分类
     *
     * @return
     */
    @Cacheable(value = {"category"},key = "'level1Category1'")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        LambdaQueryWrapper<CategoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CategoryEntity::getParentCid, 0);
        return categoryDao.selectList(wrapper);
    }

    /**
     * 用户端，查出所有分类
     *
     * @return
     */
    private List<CategoryEntity> getParent_cid(List<CategoryEntity> list, Long parent_cid) {
        return list.stream().filter(item -> item.getParentCid() == parent_cid).toList();
    }

    /**
     * 从缓存查，没有从getCatalogJsonFromDb获取
     * 1. 空结果缓存，解决缓存穿透问题。
     * 2. 设置过期时间(随机)，解决缓存雪崩问题。
     * 3. 加锁，解决缓存击穿问题。
     *
     * @return
     */
    //TODO: 压力测试时，产生堆外内存异常：OutOfDirectMemoryError
    //1) springboot2.0默认使用lettuce作为操作redis的客户端，它使用netty进行网络通信
    //2) lettuce的bug导致netty堆外内存溢出 -Xmx300m: netty如果没有指定堆外内存，默认使用-Xmx300m
    //3) 可以通过 -Dio.netty.maxDirectMemory进行设置
    // 解决方案：不能使用-Dio.netty.maxDirectMemory只去调大堆外内存。
    // 1)、升级lettuce客户端
    // 2)、切换使用jedis
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        String catalogJson = redisTemplate.opsForValue().get("catalogJsonFromDb");
        if (StringUtils.isEmpty(catalogJson)) {
            Map<String, List<Catalog2Vo>> catalogJsonFromDb = getCatalogJsonFromDb();
//            redisTemplate.opsForValue().set("catalogJsonFromDb", JSON.toJSONString(catalogJsonFromDb));
            return catalogJsonFromDb;
        }
//        StringEscapeUtils.unescapeJava(catalogJson);
        Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<>() {
        });
        return result;
    }

    /**
     * 从数据库查所有分类，并封装
     *
     * @return
     */

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDb() {
        /**
         * 程序逻辑优化
         *      1.将数据库的多次查询变为一次
         */
        /**
         * 加锁：
         *  只要是同一把锁，就能锁住需要这个锁的所有线程
         *  1. synchronized (this)：SpringBoot所有的组件在容器中都是单例的
         *  2. 在分布式情况下，必须使用分布式锁 redisson
         */
        /**
         * 缓存数据如何和数据库保持一致
         * 缓存数据一致性
         * 1）、双写模式
         * 2）、失效模式
         */
        //获取分布式锁,锁的粒度，越细越快
        //锁的粒度：具体缓存的是某个数据，11-号商品：product-11-lock
        RLock lock = redissonClient.getLock("getCatalogJson-lock");
        lock.lock();
        Map<String, List<Catalog2Vo>> collect = null;
        try {
            //得到锁后，应该再去缓存中确定一次，如果没有才继续查询
            String catalogJsonFromDb = redisTemplate.opsForValue().get("catalogJsonFromDb");
            if (!StringUtils.isEmpty(catalogJsonFromDb)) {
                return JSON.parseObject(catalogJsonFromDb, new TypeReference<Map<String, List<Catalog2Vo>>>() {
                });
            }
            List<CategoryEntity> categoryEntities = categoryDao.selectList(null);
            //1.查出所有一级分类
            List<CategoryEntity> level1Categorys = getParent_cid(categoryEntities, 0L);
            //2.封装数据
            collect = level1Categorys.stream()
                    .collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                        //每一个的一级分类，查到这个一级分类的2级分类
                        List<CategoryEntity> category = getParent_cid(categoryEntities, v.getCatId());
                        //封装上面的结果
                        List<Catalog2Vo> catalog2Vos = null;
                        if (category != null) {
                            catalog2Vos = category.stream()
                                    .map(l2 -> {
                                        Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                                        //找当前2级分类的三级分类封装成vo
                                        List<CategoryEntity> level3 = getParent_cid(categoryEntities, l2.getCatId());
                                        if (level3 != null) {
                                            List<Catalog2Vo.Catalog3Vo> catelog3List = level3.stream().map(l3 -> new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName())).toList();
                                            catalog2Vo.setCatalog3List(catelog3List);
                                        }
                                        return catalog2Vo;
                                    }).toList();
                        }
                        return catalog2Vos;
                    }));
            redisTemplate.opsForValue().set("catalogJsonFromDb", JSON.toJSONString(collect), 1, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("出错了："+e.getMessage());
        } finally {
            lock.unlock();
        }
        return collect;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbSynchronized() {
        /**
         * 程序逻辑优化
         *      1.将数据库的多次查询变为一次
         *
         */
        /**
         * 加锁：
         *  只要是同一把锁，就能锁住需要这个锁的所有线程
         *  1. synchronized (this)：SpringBoot所有的组件在容器中都是单例的
         *  TODO： 本地锁 synchronized,JUC(lock)，在分布式情况下，必须使用分布式锁
         */
        synchronized (this) {
            //得到锁后，应该再去缓存中确定一次，如果没有才继续查询
            String catalogJsonFromDb = redisTemplate.opsForValue().get("catalogJsonFromDb");
            if (!StringUtils.isEmpty(catalogJsonFromDb)) {
                return JSON.parseObject(catalogJsonFromDb, new TypeReference<Map<String, List<Catalog2Vo>>>() {
                });
            }
            List<CategoryEntity> categoryEntities = categoryDao.selectList(null);
            //1.查出所有一级分类
            List<CategoryEntity> level1Categorys = getParent_cid(categoryEntities, 0L);
            //2.封装数据
            Map<String, List<Catalog2Vo>> collect = level1Categorys.stream()
                    .collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                        //每一个的一级分类，查到这个一级分类的2级分类
                        List<CategoryEntity> category = getParent_cid(categoryEntities, v.getCatId());
                        //封装上面的结果
                        List<Catalog2Vo> catalog2Vos = null;
                        if (category != null) {
                            catalog2Vos = category.stream()
                                    .map(l2 -> {
                                        Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                                        //找当前2级分类的三级分类封装成vo
                                        List<CategoryEntity> level3 = getParent_cid(categoryEntities, l2.getCatId());
                                        if (level3 != null) {
                                            List<Catalog2Vo.Catalog3Vo> catelog3List = level3.stream().map(l3 -> new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName())).toList();
                                            catalog2Vo.setCatalog3List(catelog3List);
                                        }
                                        return catalog2Vo;
                                    }).toList();
                        }
                        return catalog2Vos;
                    }));
            redisTemplate.opsForValue().set("catalogJsonFromDb", JSON.toJSONString(collect), 1, TimeUnit.DAYS);
            return collect;
        }
    }

}
