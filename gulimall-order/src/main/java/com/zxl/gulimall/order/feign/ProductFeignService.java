package com.zxl.gulimall.order.feign;

import com.zxl.gulimall.order.vo.SpuInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    @GetMapping("/product/spuinfo/getSpuInfoBySkuId/{skuId}")
    SpuInfoVo getSpuInfoBySkuId(@PathVariable Long skuId);
    @GetMapping("/product/brand/getBrandNameById/{brandId}")
    String getBrandNameById(@PathVariable Long brandId);
}
