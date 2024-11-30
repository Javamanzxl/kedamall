package com.zxl.gulimall.member.exception;

/**
 * @author ：zxl
 * @Description: 用户名存在异常
 * @ClassName: UserNameExistException
 * @date ：2024/11/27 11:31
 */
public class UserNameExistException extends RuntimeException {
    public UserNameExistException() {
        super("用户名已经存在");
    }
}
