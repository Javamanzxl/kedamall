package com.zxl.common.to;

import lombok.Data;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: SkuHasStockTo
 * @date ：2024/11/14 17:52
 */
@Data
public class SkuHasStockTo {
    private Long SkuId;
    private Boolean hasStock;
}
