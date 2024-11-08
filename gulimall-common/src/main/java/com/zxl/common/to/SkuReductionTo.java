package com.zxl.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: SkuReductionTo
 * @date ：2024/11/06 16:57
 */
@Data
public class SkuReductionTo {
    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;

    private List<MemberPrice> memberPrice;



}
