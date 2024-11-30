package com.zxl.gulimall.member.exception;

/**
 * @author ：zxl
 * @Description: 手机号存在异常
 * @ClassName: PhoneExistException
 * @date ：2024/11/27 11:34
 */
public class PhoneExistException extends RuntimeException{
    public PhoneExistException() {
        super("手机号已经存在");
    }
}
