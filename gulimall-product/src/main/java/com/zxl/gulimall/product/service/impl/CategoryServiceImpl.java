package com.zxl.gulimall.product.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxl.gulimall.product.service.CategoryBrandRelationService;
import com.zxl.gulimall.product.vo.Catalog2Vo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
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
     *
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        categoryDao.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            //更新categoryBrandRelation表的数据
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
            //TODO:更新其他级联表
        }
    }

    /**
     * 用户端，查询出所有一级分类
     *
     * @return
     */
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
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        /**
         * 程序逻辑优化
         *      1.将数据库的多次查询变为一次
         *
         */
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
        return collect;
    }

}
