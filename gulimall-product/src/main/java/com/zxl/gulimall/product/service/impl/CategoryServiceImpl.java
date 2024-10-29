package com.zxl.gulimall.product.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxl.gulimall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

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
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        categoryDao.updateById(category);
        if(!StringUtils.isEmpty(category.getName())){
            //更新categoryBrandRelation表的数据
            categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
            //TODO:更新其他级联表
        }
    }
}