package com.zxl.gulimall.cart.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author ：zxl
 * @Description: 购物项
 * @ClassName: CartItem
 * @date ：2024/11/29 12:44
 */

@Setter
public class CartItem {
    @Getter
    private Long skuId;
    @Getter
    private boolean check = true;
    @Getter
    private String title;
    @Getter
    private String image;
    @Getter
    private List<String> skuAttr;
    @Getter
    private BigDecimal price;
    @Getter
    private Integer count;
    private BigDecimal totalPrice;

    public BigDecimal getTotalPrice() {
        return new BigDecimal("" + this.count).multiply(this.price);
    }


}
