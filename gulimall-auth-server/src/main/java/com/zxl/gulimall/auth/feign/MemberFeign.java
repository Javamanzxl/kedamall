package com.zxl.gulimall.auth.feign;

import com.zxl.common.to.MemberRegisterTo;
import com.zxl.common.to.MemberTo;
import com.zxl.common.utils.R;
import com.zxl.gulimall.auth.vo.UserLoginVo;
import com.zxl.gulimall.auth.vo.WeiboUserVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeign {

    @PostMapping("/member/member/register")
    R register(@RequestBody MemberRegisterTo registerTo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo member);

    @PostMapping("/member/member/oauth2/login")
    MemberTo oauthLogin(@RequestBody WeiboUserVo weiboUser);
}
