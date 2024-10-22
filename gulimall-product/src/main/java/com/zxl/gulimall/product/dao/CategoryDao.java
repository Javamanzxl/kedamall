package com.zxl.gulimall.product.dao;

import com.zxl.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:38:33
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
