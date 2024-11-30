package com.zxl.gulimall.cart.interceptor;

import com.zxl.common.constant.AuthServerConstant;
import com.zxl.common.constant.CartConstant;
import com.zxl.common.to.MemberTo;
import com.zxl.gulimall.cart.to.UserInfoTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @author ：zxl
 * @Description: 拦截器，在执行方法之前，判断用户登录状态。
 * 并封装传递给controller目标请求
 * @ClassName: CartInterceptor
 * @date ：2024/11/29 13:21
 */
@Component
public class CartInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        MemberTo member = (MemberTo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        UserInfoTo userInfo = new UserInfoTo();
        if (member == null) {
            //用户没登陆
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    String cookieName = cookie.getName();
                    if (cookieName.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                        userInfo.setUserKey(cookie.getValue());
                    }
                }
                if (userInfo.getUserKey() == null) {
                    String userKey = UUID.randomUUID().toString().replace("_", "");
                    userInfo.setUserKey(userKey);
                    Cookie cookie1 = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userKey);
                    cookie1.setDomain("zxl1027.com");
                    cookie1.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
                    response.addCookie(cookie1);
                }
            }

        } else {
            userInfo.setUserId(member.getId());
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    String cookieName = cookie.getName();
                    if (cookieName.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                        userInfo.setUserKey(cookie.getValue());
                    }
                }
            }
        }
        //如果没有临时用户分配一个临时用户
//        if(StringUtils.isEmpty(userInfo.getUserKey())){
//            userInfo.setUserKey(UUID.randomUUID().toString().replace("_",""));
//        }
        threadLocal.set(userInfo);
        return true;
    }

}
