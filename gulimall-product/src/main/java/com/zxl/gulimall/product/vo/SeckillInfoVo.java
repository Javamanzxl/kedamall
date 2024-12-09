package com.zxl.gulimall.product.vo;

import com.zxl.gulimall.product.entity.SkuInfoEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: SeckillInfoVo
 * @date ：2024/12/08 17:59
 */
@Data
public class SeckillInfoVo {
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;
    //当前秒杀开始和结束时间
    private Long startTime;
    private Long endTime;
    //秒杀随机码
    private String randomCode;
}
