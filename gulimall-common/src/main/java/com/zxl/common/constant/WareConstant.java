package com.zxl.common.constant;

import lombok.Getter;

/**
 * @author ：zxl
 * @Description: 库存常量
 * @ClassName: WareConstant
 * @date ：2024/11/07 20:38
 */
public class WareConstant {
    @Getter
    public enum PurchaseStatusEnum{
        CREATED(0,"新建"),
        ASSIGNED(1,"已分配"),
        RECEIVE(2,"已领取"),
        FINISH(3,"已完成"),
        HAS_ERROR(4,"有异常");
        private final int code;
        private final String msg;
        PurchaseStatusEnum(Integer code,String msg){
            this.code = code;
            this.msg = msg;
        }
    }

    @Getter
    public enum PurchaseDetailStatusEnum{
        CREATED(0,"新建"),
        ASSIGNED(1,"已分配"),
        BUYING(2,"正在采购"),
        FINISH(3,"已完成"),
        HAS_ERROR(4,"采购失败");
        private final int code;
        private final String msg;
        PurchaseDetailStatusEnum(Integer code,String msg){
            this.code = code;
            this.msg = msg;
        }
    }
}
