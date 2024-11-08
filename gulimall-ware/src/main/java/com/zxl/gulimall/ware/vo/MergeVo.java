package com.zxl.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: MergeVo
 * @date ：2024/11/07 20:31
 */
@Data
public class MergeVo {
    private Long purchaseId;
    private List<Long> items;
}
