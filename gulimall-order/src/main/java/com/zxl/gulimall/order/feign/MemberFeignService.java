package com.zxl.gulimall.order.feign;

import com.zxl.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: memberFeignService
 * @date ：2024/12/02 15:29
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {
    @GetMapping("/member/memberreceiveaddress/{memberId}/getAddress")
    List<MemberAddressVo> getAddress(@PathVariable Long memberId);
}
