package com.zxl.gulimall.seckill.feign;

import com.zxl.common.to.SkuInfoTo;
import com.zxl.gulimall.seckill.vo.SkuInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    @RequestMapping("/product/skuinfo/infoBySkuId")
    SkuInfoVo infoBySkuId(@RequestParam Long skuId);
}
