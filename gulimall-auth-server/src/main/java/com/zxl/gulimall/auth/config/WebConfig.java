package com.zxl.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author ：zxl
 * @Description: 页面配置
 * @ClassName: WebConfig
 * @date ：2024/11/26 16:15
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    /**
     * 视图映射
     * @param registry
     */

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/reg.html").setViewName("reg");
    }
}
