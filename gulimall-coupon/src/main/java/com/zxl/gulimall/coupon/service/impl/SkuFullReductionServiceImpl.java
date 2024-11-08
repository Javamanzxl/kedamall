package com.zxl.gulimall.coupon.service.impl;

import com.zxl.common.to.MemberPrice;
import com.zxl.common.to.SkuReductionTo;
import com.zxl.gulimall.coupon.entity.MemberPriceEntity;
import com.zxl.gulimall.coupon.entity.SkuLadderEntity;
import com.zxl.gulimall.coupon.service.MemberPriceService;
import com.zxl.gulimall.coupon.service.SkuLadderService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.Query;

import com.zxl.gulimall.coupon.dao.SkuFullReductionDao;
import com.zxl.gulimall.coupon.entity.SkuFullReductionEntity;
import com.zxl.gulimall.coupon.service.SkuFullReductionService;

import javax.annotation.Resource;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Resource
    private SkuLadderService skuLadderService;

    @Resource
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //sku的优惠、满减等信息:gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price\
        //sms_sku_ladder
        SkuLadderEntity skuLadder = new SkuLadderEntity();
        skuLadder.setSkuId(skuReductionTo.getSkuId());
        skuLadder.setFullCount(skuReductionTo.getFullCount());
        skuLadder.setDiscount(skuReductionTo.getDiscount());
        skuLadder.setAddOther(skuReductionTo.getCountStatus());
        if (skuReductionTo.getFullCount() > 0) {
            skuLadderService.save(skuLadder);
        }
        //sms_sku_full_reduction
        SkuFullReductionEntity skuFullReduction = new SkuFullReductionEntity();
        skuFullReduction.setSkuId(skuReductionTo.getSkuId());
        skuFullReduction.setFullPrice(skuReductionTo.getFullPrice());
        skuFullReduction.setReducePrice(skuReductionTo.getReducePrice());
        skuFullReduction.setAddOther(skuReductionTo.getCountStatus());
        if(skuReductionTo.getFullPrice().compareTo(new BigDecimal("0"))==1){
            this.save(skuFullReduction);
        }
        //sms_member_price
        List<MemberPrice> memberPrices = skuReductionTo.getMemberPrice();
        if (memberPrices != null && !memberPrices.isEmpty()) {
            List<MemberPriceEntity> memberPriceEntities = memberPrices.stream().map((item) -> {
                MemberPriceEntity memberPrice = new MemberPriceEntity();
                memberPrice.setSkuId(skuReductionTo.getSkuId());
                memberPrice.setMemberPrice(item.getPrice());
                memberPrice.setMemberLevelId(item.getId());
                memberPrice.setMemberLevelName(item.getName());
                memberPrice.setAddOther(1);
                return memberPrice;
            }).filter(item-> item.getMemberPrice().compareTo(new BigDecimal("0"))==1).toList();
            memberPriceService.saveBatch(memberPriceEntities);
        }
    }
}