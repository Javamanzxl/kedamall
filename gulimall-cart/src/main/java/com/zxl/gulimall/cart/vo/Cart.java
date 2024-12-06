package com.zxl.gulimall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author ：zxl
 * @Description: 购物车
 * @ClassName: cart
 * @date ：2024/11/29 12:43
 */
public class Cart {
    private List<CartItem> items;
    private Integer countNum; //商品数量
    private Integer countType; //商品类型数量
    private BigDecimal totalAmount; //商品总价
    private BigDecimal reduce = new BigDecimal("0.00"); //减免价格

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if(items!=null && !items.isEmpty()){
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        int count = 0;
        if(items!=null && !items.isEmpty()){
            for (CartItem item : items) {
                count ++;
            }
        }
        return count;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        if(items!=null && !items.isEmpty()){
            for (CartItem item : items) {
                if(item.isCheck()){
                    amount = amount.add(item.getTotalPrice());
                }
            }
        }
        amount = amount.subtract(this.getReduce());
        return amount;
    }


    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
