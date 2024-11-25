package com.zxl.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: SpuItemAttrGroupVo
 * @date ：2024/11/25 17:29
 */
@Data
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<Attr> attrs;
}
