package com.zxl.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxl.gulimall.product.dao.BrandDao;
import com.zxl.gulimall.product.dao.CategoryDao;
import com.zxl.gulimall.product.entity.BrandEntity;
import com.zxl.gulimall.product.entity.CategoryEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.Query;

import com.zxl.gulimall.product.dao.CategoryBrandRelationDao;
import com.zxl.gulimall.product.entity.CategoryBrandRelationEntity;
import com.zxl.gulimall.product.service.CategoryBrandRelationService;

import javax.annotation.Resource;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Resource
    private CategoryBrandRelationDao categoryBrandRelationDao;
    @Resource
    private CategoryDao categoryDao;
    @Resource
    private BrandDao brandDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryBrandRelationEntity> catelogList(Long brandId) {
        LambdaQueryWrapper<CategoryBrandRelationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CategoryBrandRelationEntity::getBrandId,brandId);
        return categoryBrandRelationDao.selectList(wrapper);
    }

    /**
     * 查询出CategoryBrandRelationEntity的name字段，保存到数据库中
     * @param categoryBrandRelation
     */
    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        BrandEntity brand = brandDao.selectById(brandId);
        CategoryEntity category = categoryDao.selectById(catelogId);
        categoryBrandRelation.setBrandName(brand.getName());
        categoryBrandRelation.setCatelogName(category.getName());
        categoryBrandRelationDao.insert(categoryBrandRelation);

    }

    /**
     * 更新brand相关信息
     * @param brandId
     * @param name
     */
    @Override
    public void updateBrand(Long brandId, String name) {
        categoryBrandRelationDao.updateBrand(brandId,name);
    }

    /**
     * 更新category相关信息
     * @param catId
     * @param name
     */
    @Override
    public void updateCategory(Long catId, String name) {
        categoryBrandRelationDao.updateCategory(catId,name);
    }

    /**
     * 查询分类关联品牌
     * @param catId
     * @return
     */
    @Override
    public List<BrandEntity> brandsList(Long catId) {
        List<CategoryBrandRelationEntity> relations =
                categoryBrandRelationDao.selectList(new LambdaQueryWrapper<CategoryBrandRelationEntity>()
                .eq(CategoryBrandRelationEntity::getCatelogId, catId));
        List<Long> brandIds = relations.stream().map(CategoryBrandRelationEntity::getBrandId).toList();
        if(!brandIds.isEmpty()){
            return brandDao.selectBatchIds(brandIds);
        }
        return null;
    }
}