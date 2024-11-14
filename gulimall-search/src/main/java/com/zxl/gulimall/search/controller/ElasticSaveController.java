package com.zxl.gulimall.search.controller;

import com.zxl.common.exception.ErrorCodeEnum;
import com.zxl.common.to.es.SkuEsModel;
import com.zxl.common.utils.R;
import com.zxl.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author ：zxl
 * @Description: 保存服务
 * @ClassName: ElasticSaveController
 * @date ：2024/11/14 18:15
 */
@Slf4j
@RestController
@RequestMapping("/search/save")
public class ElasticSaveController {

    @Resource
    private ProductSaveService productSaveService;
    @PostMapping("product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels){
        boolean b = false;
        try{
           b  = productSaveService.productStatusUp(skuEsModels);
        }catch (Exception e){
            log.error("ElasticSaveController的productStatusUp商品上架出现错误:{}",e.getMessage());
            return R.error(ErrorCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),ErrorCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
        if(!b){
            return R.ok();
        }else{
            return R.error(ErrorCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),ErrorCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }

    }
}
