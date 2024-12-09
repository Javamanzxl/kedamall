package com.zxl.gulimall.ware.feign;

import com.zxl.common.utils.R;
import com.zxl.gulimall.ware.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: WareFeignService
 * @date ：2024/12/02 19:46
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {
    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    R info(@PathVariable("id") Long id);
}
