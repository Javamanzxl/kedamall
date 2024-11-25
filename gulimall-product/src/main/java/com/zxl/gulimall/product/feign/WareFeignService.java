package com.zxl.gulimall.product.feign;

import com.zxl.common.to.SkuHasStockTo;
import com.zxl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient("gulimall-ware")
public interface WareFeignService {
    @RequestMapping("ware/wareinfo/list")
    R list(@RequestParam Map<String, Object> params);
    @PostMapping("ware/waresku/hasStock")
    List<SkuHasStockTo> getSkusHasStock(@RequestBody List<Long> skuIds);
    @GetMapping("ware/waresku/{skuId}/hasStock")
    boolean hasStockBySkuId(@PathVariable Long skuId);

}
