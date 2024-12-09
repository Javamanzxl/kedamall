package com.zxl.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.Query;

import com.zxl.gulimall.coupon.dao.SeckillSkuRelationDao;
import com.zxl.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.zxl.gulimall.coupon.service.SeckillSkuRelationService;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<SeckillSkuRelationEntity> wrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");
        String sessionId = (String)params.get("promotionSessionId");
        if(!StringUtils.isBlank(sessionId)){
            wrapper.eq(SeckillSkuRelationEntity::getPromotionSessionId,sessionId);
        }
        if(!StringUtils.isBlank(key)){
            wrapper.like(SeckillSkuRelationEntity::getId,key).or().like(SeckillSkuRelationEntity::getPromotionId,key);
        }
        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}