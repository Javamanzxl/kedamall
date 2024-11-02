package com.zxl.common.constant;

import lombok.Getter;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: ProductConstant
 * @date ：2024/10/31 19:48
 */
public class ProductConstant {
    @Getter
    public enum AttrEnum{
        ATTR_TYPE_BASE(1,"基本属性"),
        ATTR_TYPE_SALE(0,"销售属性");
        private final Integer code;
        private final String msg;
        AttrEnum(Integer code,String msg){
            this.code = code;
            this.msg = msg;
        }

    }
}
