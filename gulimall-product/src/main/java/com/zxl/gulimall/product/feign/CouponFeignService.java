package com.zxl.gulimall.product.feign;

import com.zxl.common.to.SkuReductionTo;
import com.zxl.common.to.SpuBoundTo;
import com.zxl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: SpuFeignService
 * @date ：2024/11/06 16:35
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    /**
     * 1.CouponFeignService.saveSpuBounds(spuBoundTo);
     *      1).@RequestBody将spuBoundTo这个对象转为json
     *      2).找到gulimall-coupon这个服务，给coupon/spubounds/save发送请求
     *      将上一步转的json放在请求体位置发送请求
     *      3).对方服务收到请求。收到的是请求体的json数据
     *          @RequestBody SpuBoundsEntity spuBounds: 将请求体的json转为SpuBoundsEntity
     * 只要json数据模型是兼容的，双方服务无需使用同一个to
     * @param spuBoundTo
     * @return
     */


    @PostMapping("coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);


    @PostMapping("coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
