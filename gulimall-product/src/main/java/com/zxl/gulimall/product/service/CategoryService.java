package com.zxl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxl.common.utils.PageUtils;
import com.zxl.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:38:33
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listTree();

    void removeMenusByIdS(List<Long> list);
}

