package com.zxl.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author ：zxl
 * @Description: 订单确认页需要的数据
 * @ClassName: OrderConfirmVo
 * @date ：2024/12/02 15:16
 */

public class OrderConfirmVo {
    // 收货地址，ums_member_receive_address表
    @Getter
    @Setter
    private List<MemberAddressVo> address;
    // 商品信息,所有选中的购物项
    @Getter
    @Setter
    private List<OrderItemVo> items;
    @Getter
    @Setter
    private Map<Long,Boolean> stocks;

    //优惠劵信息....
    //会员的积分信息
    @Getter
    @Setter
    private Integer integration;
    //订单总额
    @Setter
    private BigDecimal total; //订单总额

    @Setter
    private BigDecimal payPrice; //应付价格
    @Getter
    @Setter
    private String orderToken;
    public BigDecimal getTotal(){
        BigDecimal sum = new BigDecimal("0");
        if(items!=null){
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount()+""));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }
    public BigDecimal getPayPrice(){
        return getTotal();
    }
    public Integer getCount() {
        Integer count = 0;
        if (items != null) {
            for (OrderItemVo item : items) {
                count += item.getCount();
            }
        }
        return count;
    }


}
