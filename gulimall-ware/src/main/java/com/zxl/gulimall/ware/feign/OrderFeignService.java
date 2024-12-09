package com.zxl.gulimall.ware.feign;

import com.zxl.common.to.OrderTo;
import com.zxl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-order")
public interface OrderFeignService {
    @GetMapping("/order/order/getOrder/{orderSn}")
    R getOrderByOrderSn(@PathVariable String orderSn);
}
