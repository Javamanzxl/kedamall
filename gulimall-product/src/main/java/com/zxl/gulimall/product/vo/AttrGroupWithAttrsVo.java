package com.zxl.gulimall.product.vo;

import com.zxl.gulimall.product.entity.AttrEntity;
import lombok.Data;
import java.util.List;

/**
 * @author ：zxl
 * @Description: 某一分类下属性分组的所有属性
 * @ClassName: AttrGroupWithAttrsVo
 * @date ：2024/11/02 17:00
 */
@Data
public class AttrGroupWithAttrsVo {
    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;
    /**
     * 所有属性
     */
    private List<AttrEntity> attrs;
}
