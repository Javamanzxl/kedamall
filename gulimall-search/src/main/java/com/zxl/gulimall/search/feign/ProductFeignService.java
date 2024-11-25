package com.zxl.gulimall.search.feign;

import com.zxl.common.to.AttrRespTo;
import com.zxl.common.to.BrandTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: ProductFeignService
 * @date ：2024/11/21 20:09
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/attr/feign/info/{attrId}")
    AttrRespTo feign_info(@PathVariable("attrId") Long attrId);
    @GetMapping("/product/brand/feign/infos")
    List<BrandTo> infos(@RequestParam List<Long> brandIds);


}
