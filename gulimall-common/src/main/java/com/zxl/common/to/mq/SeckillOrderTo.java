package com.zxl.common.to.mq;

import com.zxl.common.to.OrderTo;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: SeckillOrderTo
 * @date ：2024/12/09 15:36
 */
@Data
public class SeckillOrderTo {
    /**
     * 订单号
     */
    private String orderSn;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价
     */
    private BigDecimal seckillPrice;
    /**
     * 购买数量
     */
    private Integer num;
    private Long memberId;
}
