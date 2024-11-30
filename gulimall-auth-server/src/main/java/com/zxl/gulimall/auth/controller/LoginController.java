package com.zxl.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.zxl.common.constant.AuthServerConstant;
import com.zxl.common.exception.ErrorCodeEnum;
import com.zxl.common.to.MemberRegisterTo;
import com.zxl.common.to.MemberTo;
import com.zxl.common.utils.R;
import com.zxl.gulimall.auth.feign.MemberFeign;
import com.zxl.gulimall.auth.feign.ThirdPartyFeign;
import com.zxl.gulimall.auth.vo.RegisterVo;
import com.zxl.gulimall.auth.vo.UserLoginVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ：zxl
 * @Description: 登录注册功能
 * @ClassName: LoginController
 * @date ：2024/11/26 15:48
 */
@Controller
@Slf4j
public class LoginController {
    @Resource
    private ThirdPartyFeign thirdPartyFeign;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private MemberFeign memberFeign;

    /**
     * 登陆页，如果已经登录系统中存有session直接调回首页，没有跳到登录页
     * @return
     */
    @GetMapping("/login.html")
    public String LoginPage(HttpSession session){
        Object loginUser = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(loginUser==null){
            return "login";
        }else{
            return "redirect:http://zxl1027.com";
        }

    }

    /**
     * 发送验证码功能
     *
     * @param phone
     * @return
     */
    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000) {
                //60秒不能再次发送验证码
                return R.error(ErrorCodeEnum.SMS_CODE_EXCEPTION.getCode(), ErrorCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }
        //1.接口防刷
        //2.验证码的再次校验
        Random random = new Random();
        // 生成五位数随机数
        int i = 10000 + random.nextInt(90000);
        String code = String.valueOf(i);
        //redis缓存验证码+存入时间，并且防止同一个phone在60秒内再次发送验证码
        String codeTime = code + "_" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, codeTime, 5, TimeUnit.MINUTES);
        log.info("code:{}", code);
        thirdPartyFeign.sendSms(phone, code);
        return R.ok();
    }

    /**
     * 注册功能
     * @param registerVo
     * @param result
     * @param redirectAttributes
     * @return
     */

    //TODO: 重定向携带数据，利用session原理，将数据放在session中，只要跳到下一个页面取出这个数据以后，session里面数据就会删掉
    //TODO: 这样就会涉及分布式session问题
    @PostMapping("/register")
    public String register(@Valid RegisterVo registerVo, BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Map<String, String> error = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", error);
            //校验出错转发到注册页
            //Request method 'POST' not supported
            //用户注册 -> /regist[post] -> 转发/reg.html(路径映射默认都是get方式访问的)
            //但是   return "forward:/reg.html"; 转发还是用的post请求
            return "redirect:http://auth.zxl1027.com/reg.html";
        }
        //校验验证码后调用远程服务进行注册
        String code = registerVo.getCode();
        String phone = registerVo.getPhone();
        String redisValue = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisValue)) {
            String redisCode = redisValue.split("_")[0];
            if (redisCode.equals(code)) {
                //与member服务端信息进行校验查询是否有相同信息
                MemberRegisterTo memberRegisterTo = new MemberRegisterTo();
                BeanUtils.copyProperties(registerVo,memberRegisterTo);
                //调用远程服务进行注册
                R r = memberFeign.register(memberRegisterTo);
                if(r.getCode() == 0){
                    //成功
                    //删除验证码
                    redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
                    //注册成功返回首页
                    return "redirect:http://auth.zxl1027.com/login.html";
                }else{
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg",r.getMessage());
                    redirectAttributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.zxl1027.com/reg.html";
                }


            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.zxl1027.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.zxl1027.com/reg.html";
        }

    }

    /**
     * 登录功能
     * @param user
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/login")
    public String login(UserLoginVo user, RedirectAttributes redirectAttributes, HttpSession session){
        R r = memberFeign.login(user);
        if(r.getCode()==0){
            MemberTo memberTo = r.getData2("data", new TypeReference<MemberTo>(){});
            session.setAttribute(AuthServerConstant.LOGIN_USER,memberTo);
            return "redirect:http://zxl1027.com";
        }else{
            Map<String, String> errors = new HashMap<>();
            errors.put("msg",r.getMessage());
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.zxl1027.com/login.html";
        }

    }
}
