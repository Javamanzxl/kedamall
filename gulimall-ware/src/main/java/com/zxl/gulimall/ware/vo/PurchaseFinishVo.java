package com.zxl.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author ：zxl
 * @Description: 采购单完成vo
 * @ClassName: PurchaseFinishVo
 * @date ：2024/11/08 15:40
 */
@Data
public class PurchaseFinishVo {
    @NotNull
    private Long id;//采购单id
    private List<PurchaseItemFinishVo> items;
}
