package com.zxl.gulimall.order.vo;

import lombok.Data;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: LockResultVo
 * @date ：2024/12/03 17:32
 */
@Data
public class LockStockResultVo {
    private Long skuId;
    private Integer num;
    private Boolean locked;
}
