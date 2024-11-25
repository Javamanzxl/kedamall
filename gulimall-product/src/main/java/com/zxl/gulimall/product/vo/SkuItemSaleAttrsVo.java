package com.zxl.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: SkuItemSaleAttrsVo
 * @date ：2024/11/25 17:35
 */
@Data
public class SkuItemSaleAttrsVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}
