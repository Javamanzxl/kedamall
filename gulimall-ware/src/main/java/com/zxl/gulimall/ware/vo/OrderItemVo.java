package com.zxl.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: OrderItemVo
 * @date ：2024/12/02 15:23
 */
@Data
public class OrderItemVo {

    private Long skuId;

    private String title;

    private String image;

    private List<String> skuAttr;

    private BigDecimal price;

    private Integer count;
    private BigDecimal totalPrice;
    private BigDecimal weight;
}
