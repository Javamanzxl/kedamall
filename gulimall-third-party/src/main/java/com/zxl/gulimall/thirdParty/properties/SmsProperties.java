package com.zxl.gulimall.thirdParty.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: SmsProperties
 * @date ：2024/11/26 17:56
 */
@ConfigurationProperties(prefix = "spring.alicloud.sms")
@Data
public class SmsProperties {

    private String host;

    private String path;

    private String appcode;

    private String smsSignId;

    private String templateId;
}
