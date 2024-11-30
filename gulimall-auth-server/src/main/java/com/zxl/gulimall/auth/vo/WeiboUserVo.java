package com.zxl.gulimall.auth.vo;

import lombok.Data;

/**
 * @author ：zxl
 * @Description: 封装微博认证返回的AccessToken数据
 * @ClassName: WeiboAccessToeknVo
 * @date ：2024/11/27 20:50
 */
@Data
public class WeiboUserVo {
    private String access_token;
    private Long remind_in;
    private Long expires_in;
    private String uid;
}
