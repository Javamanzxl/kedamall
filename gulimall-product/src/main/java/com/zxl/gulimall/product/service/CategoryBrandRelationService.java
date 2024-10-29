package com.zxl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxl.common.utils.PageUtils;
import com.zxl.gulimall.product.entity.CategoryBrandRelationEntity;
import com.zxl.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:38:33
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryBrandRelationEntity> catelogList(Long brandId);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(Long brandId, String name);


    void updateCategory(Long catId, String name);
}

