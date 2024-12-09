package com.zxl.gulimall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: WareSkuLockVo
 * @date ：2024/12/03 17:27
 */
@Data
public class WareSkuLockVo {
    private String orderSn;
    private List<OrderItemVo> locks; //需要锁的库存信息
}
