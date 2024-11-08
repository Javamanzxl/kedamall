package com.zxl.gulimall.ware.feign;

import com.zxl.common.to.SkuInfoTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-product")
public interface ProductFeign {
    @RequestMapping("product/skuinfo/infoBySkuId/")
    SkuInfoTo infoBySkuId(@RequestParam Long skuId);
}
