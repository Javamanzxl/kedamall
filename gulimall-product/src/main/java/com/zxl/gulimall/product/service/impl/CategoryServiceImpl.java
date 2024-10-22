package com.zxl.gulimall.product.service.impl;


import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.Query;

import com.zxl.gulimall.product.dao.CategoryDao;
import com.zxl.gulimall.product.entity.CategoryEntity;
import com.zxl.gulimall.product.service.CategoryService;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryDao categoryDao;

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
                }).sorted(Comparator.comparingInt(CategoryEntity::getSort)).toList();
        return children;
    }

    @Override
    public void removeMenusByIdS(List<Long> list) {
        //TODO 1.检查当前删除的菜单，是否被其他地方引用
        categoryDao.deleteBatchIds(list);
    }
}