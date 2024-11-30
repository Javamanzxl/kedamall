package com.zxl.gulimall.auth.vo;

import lombok.Data;

/**
 * @author ：zxl
 * @Description: 用户登录vo
 * @ClassName: UserLoginVo
 * @date ：2024/11/27 13:06
 */
@Data
public class UserLoginVo {
    private String loginAccount;
    private String password;
}
