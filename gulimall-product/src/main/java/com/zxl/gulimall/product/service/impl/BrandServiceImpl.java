package com.zxl.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxl.gulimall.product.dao.CategoryBrandRelationDao;
import com.zxl.gulimall.product.entity.CategoryBrandRelationEntity;
import com.zxl.gulimall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.Query;

import com.zxl.gulimall.product.dao.BrandDao;
import com.zxl.gulimall.product.entity.BrandEntity;
import com.zxl.gulimall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {
    @Resource
    private BrandDao brandDao;
    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        LambdaQueryWrapper<BrandEntity> wrapper = new LambdaQueryWrapper<>();
        IPage<BrandEntity> page;
        if (!StringUtils.isEmpty(key)) {
            wrapper.like(BrandEntity::getName, key);
        }
        page = this.page(new Query<BrandEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public void updateStatus(Long brandId, Integer showStatus) {
        brandDao.updateStatus(brandId, showStatus);
    }

    /**
     * 更新brand表和CategoryBrandRelation表的相关内容
     * @param brand
     */
    @Transactional
    @Override
    public void updateCascade(BrandEntity brand) {
        //保证冗余字段的数据一直
        brandDao.updateById(brand);
        if(!StringUtils.isEmpty(brand.getName())){
            //同步更新其他关联表的数据
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());
            //TODO 更新其他关联数据
        }

    }

    /**
     * feign查brand
     * @param brandIds
     * @return
     */
    @Override
    public List<BrandEntity> getBrandIds(List<Long> brandIds) {
        LambdaQueryWrapper<BrandEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BrandEntity::getBrandId,brandIds);
        return brandDao.selectList(wrapper);
    }
}