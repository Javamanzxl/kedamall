package com.zxl.gulimall.product.web;

import com.zxl.gulimall.product.service.SkuInfoService;
import com.zxl.gulimall.product.vo.SkuItemVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * @author ：zxl
 * @Description: 商品详情
 * @ClassName: itemController
 * @date ：2024/11/25 12:20
 */
@Controller
public class ItemController {
    @Resource
    private SkuInfoService skuInfoService;
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable Long skuId, Model model) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        model.addAttribute("item",skuItemVo);
        return "item";
    }
}
