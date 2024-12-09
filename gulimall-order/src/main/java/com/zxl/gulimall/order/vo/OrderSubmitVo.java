package com.zxl.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ：zxl
 * @Description: 封装订单提交的数据
 * @ClassName: OrderSubmitVo
 * @date ：2024/12/02 21:16
 */
@Data
public class OrderSubmitVo {
    private Long addrId; //收货地址id
    private Integer payType; //支付方式
    //无需提交需要购买的商品，去购物车服务再获取一遍
    //优惠发票等
    private String orderToken; //防重令牌
    private BigDecimal payPrice; //应付总额
    //用户信息在session中，不用提交
    private String note; //订单备注
}
