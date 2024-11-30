package com.zxl.gulimall.cart.to;

import lombok.Data;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: UserInfoVo
 * @date ：2024/11/29 13:28
 */
@Data
public class UserInfoTo {
    private Long userId;
    private String userKey;
    private boolean tempUser = false;
}
