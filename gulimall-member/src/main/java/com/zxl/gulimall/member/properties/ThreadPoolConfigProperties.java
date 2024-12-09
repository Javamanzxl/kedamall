package com.zxl.gulimall.member.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ：zxl
 * @Description: 线程池配置文件
 * @ClassName: ThreadPoolConfigProperties
 * @date ：2024/11/25 20:48
 */
@ConfigurationProperties(prefix = "gulimall.thread")
@Data
public class ThreadPoolConfigProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
