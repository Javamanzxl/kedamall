package com.zxl.gulimall.auth.feign;

import com.zxl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author ：zxl
 * @Description: 第三方服务远程调用
 * @ClassName: ThridPatyFeign
 * @date ：2024/11/26 18:55
 */
@FeignClient("gulimall-third-party")
public interface ThirdPartyFeign {

    @GetMapping("/sms/sendCode")
    R sendSms(@RequestParam("phone")String phone, @RequestParam ("code")String code);
}
