package com.zxl.gulimall.ware.vo;

import lombok.Data;

/**
 * @author ：zxl
 * @Description: 采购项Vo
 * @ClassName: ItemVo
 * @date ：2024/11/08 15:37
 */
@Data
public class PurchaseItemFinishVo {
    private Long itemId;
    private Integer status;
    private String reason;

}
