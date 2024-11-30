package com.zxl.gulimall.cart.config;

import com.zxl.gulimall.cart.interceptor.CartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author ：zxl
 * @Description: WebMvc配置类
 * @ClassName: WebMvcConfig
 * @date ：2024/11/29 13:23
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private CartInterceptor cartInterceptor;
    /**
     * 拦截器注册
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cartInterceptor).addPathPatterns("/**");
    }
}
