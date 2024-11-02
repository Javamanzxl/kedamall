package com.zxl.gulimall.product.vo;

import lombok.Data;

/**
 * @author ：zxl
 * @Description: 响应ov
 * @ClassName: AttrRespVo
 * @date ：2024/10/31 16:48
 */
@Data
public class AttrRespVo extends AttrVo{
    /**
     * 所属于分类名字
     */
    private String catelogName;
    /**
     * 所属属性分组名称
     */
    private String groupName;
    private Long[] catelogPath;
}
