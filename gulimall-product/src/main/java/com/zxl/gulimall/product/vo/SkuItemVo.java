package com.zxl.gulimall.product.vo;

import com.zxl.gulimall.product.entity.SkuImagesEntity;
import com.zxl.gulimall.product.entity.SkuInfoEntity;
import com.zxl.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @author ：zxl
 * @Description: 用户端商品详情封装数据
 * @ClassName: SkuItemVo
 * @date ：2024/11/25 15:03
 */
@Data
public class SkuItemVo {
    private SkuInfoEntity skuInfo;
    private boolean hasStock = true;
    private List<SkuImagesEntity> skuImages;
    private List<SkuItemSaleAttrsVo> saleAttrs;
    private SpuInfoDescEntity spuInfoDesc;
    private List<SpuItemAttrGroupVo> attrGroups;
    //当前商品的秒杀信息
    private SeckillInfoVo seckillInfo;

}
