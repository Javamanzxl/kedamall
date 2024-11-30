package com.zxl.gulimall.product.interceptor;

import com.zxl.common.constant.CartConstant;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @author ：zxl
 * @Description: 添加用户UserKey标识拦截器
 * @ClassName: UserKeyInterceptor
 * @date ：2024/11/30 10:35
 */
@Component
public class UserKeyInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                String cookieName = cookie.getName();
                if (cookieName.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    return true;
                }
            }
        } else {
            String userKey = UUID.randomUUID().toString().replace("_", "");
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userKey);
            cookie.setDomain("zxl1027.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
            return true;
        }
        return true;
    }
}
