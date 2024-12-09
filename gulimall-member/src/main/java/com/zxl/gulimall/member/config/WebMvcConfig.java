package com.zxl.gulimall.member.config;


import com.zxl.gulimall.member.interceptor.LoginUserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: WebMvcConfig
 * @date ：2024/12/01 21:10
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private LoginUserInterceptor loginUserInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInterceptor)
                .addPathPatterns("/**");
    }
}
