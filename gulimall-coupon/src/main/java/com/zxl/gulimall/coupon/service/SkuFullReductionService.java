package com.zxl.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxl.common.to.SkuReductionTo;
import com.zxl.common.utils.PageUtils;
import com.zxl.gulimall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:39:32
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo);
}

