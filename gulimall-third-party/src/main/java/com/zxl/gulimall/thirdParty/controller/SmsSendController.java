package com.zxl.gulimall.thirdParty.controller;

import com.zxl.common.utils.R;
import com.zxl.gulimall.thirdParty.component.SmsComponent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author ：zxl
 * @Description: 发送验证码
 * @ClassName: SmsSendController
 * @date ：2024/11/26 18:46
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {
    @Resource
    private SmsComponent sms;
    @GetMapping("/sendCode")
    public R sendSms(@RequestParam ("phone")String phone, @RequestParam ("code")String code){
        sms.sendSms(phone,code);
        return R.ok();
    }
}
