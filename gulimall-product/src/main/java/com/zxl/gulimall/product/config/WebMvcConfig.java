package com.zxl.gulimall.product.config;

import com.zxl.gulimall.product.interceptor.UserKeyInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: WebMvcConfig
 * @date ：2024/11/30 10:41
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private UserKeyInterceptor userKeyInterceptor;

    /**
     * 拦截器注册
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userKeyInterceptor).addPathPatterns("/**");
    }
}
