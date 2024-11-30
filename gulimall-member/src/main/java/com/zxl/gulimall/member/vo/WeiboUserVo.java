package com.zxl.gulimall.member.vo;

import lombok.Data;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: WeiboUserVo
 * @date ：2024/11/27 21:08
 */
@Data
public class WeiboUserVo {
    private String access_token;
    private Long remind_in;
    private Long expires_in;
    private String uid;
}
