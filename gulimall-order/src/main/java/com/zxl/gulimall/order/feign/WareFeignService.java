package com.zxl.gulimall.order.feign;

import com.zxl.common.to.SkuHasStockTo;
import com.zxl.common.utils.R;
import com.zxl.gulimall.order.vo.SkuHasStockVo;
import com.zxl.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasStock")
    List<SkuHasStockVo> getSkusHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("/ware/wareinfo/getFare")
    R getFare(@RequestParam("addrId") Long attrId);

    @PostMapping("/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);
}
