package com.zxl.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCodeEnum {
    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VAILD_EXCEPTION(10001,"参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002,"短信验证码获取频率太高，稍后再试"),
    USER_EXIST_EXCEPTION(15001,"用户名已经存在"),
    PHONE_EXIST_EXCEPTION(15002,"手机号已经存在"),
    ACCOUNT_PASSWORD_EXCEPTION(15003,"账号或密码错误"),
    NO_STOCK_EXCEPTION(21000,"商品库存不足"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常");

    private final Integer code;
    private final String message;
    ErrorCodeEnum(Integer code,String message){
        this.code = code;
        this.message = message;
    }


}
