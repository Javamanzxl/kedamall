package com.zxl.gulimall.order.to;

import com.zxl.gulimall.order.entity.OrderEntity;
import com.zxl.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: OrderCreateTo
 * @date ：2024/12/03 10:54
 */
@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItem;
    private BigDecimal payPrice; //应付价格
    private BigDecimal fare; //运费
}
