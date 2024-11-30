package com.zxl.gulimall.auth.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ：zxl
 * @Description: 微博社交登陆信息
 * @ClassName: WeiboProperties
 * @date ：2024/11/27 20:30
 */
@Data
@ConfigurationProperties(prefix = "oauth2.weibo")
public class WeiboProperties {
    private String clientId;
    private String clientSecret;
    private String registeredRedirectUri;
}
