package com.zxl.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCodeEnum {
    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VAILD_EXCEPTION(10001,"参数格式校验失败"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常");
    private final Integer code;
    private final String message;
    ErrorCodeEnum(Integer code,String message){
        this.code = code;
        this.message = message;
    }


}
