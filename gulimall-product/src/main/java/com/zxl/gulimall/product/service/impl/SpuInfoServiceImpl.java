package com.zxl.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxl.common.constant.ProductConstant;
import com.zxl.common.to.MemberPrice;
import com.zxl.common.to.SkuHasStockTo;
import com.zxl.common.to.SkuReductionTo;
import com.zxl.common.to.SpuBoundTo;
import com.zxl.common.to.es.SkuEsModel;
import com.zxl.common.utils.R;
import com.zxl.gulimall.product.dao.*;
import com.zxl.gulimall.product.entity.*;
import com.zxl.gulimall.product.feign.CouponFeignService;
import com.zxl.gulimall.product.feign.SearchFeignService;
import com.zxl.gulimall.product.feign.WareFeignService;
import com.zxl.gulimall.product.service.*;
import com.zxl.gulimall.product.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.Query;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.swing.*;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Resource
    private SpuInfoDao spuInfoDao;
    @Resource
    private SpuInfoDescDao spuInfoDescDao;
    @Resource
    private SpuImagesService spuImagesService;
    @Resource
    private ProductAttrValueService productAttrValueService;
    @Resource
    private AttrService attrService;
    @Resource
    private SkuInfoService skuInfoService;
    @Resource
    private SkuImagesService skuImagesService;
    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Resource
    private CouponFeignService couponFeignService;
    @Resource
    private BrandDao brandDao;
    @Resource
    private CategoryDao categoryDao;
    @Resource
    private WareFeignService wareFeignService;
    @Resource
    private SearchFeignService searchFeignService;

    /**
     * 条件查询
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<SpuInfoEntity> wrapper = new LambdaQueryWrapper<>();
        if (!params.isEmpty()) {
            String brandId = (String) params.get("brandId");
            if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
                wrapper.eq(SpuInfoEntity::getBrandId, brandId);
            }
            String catelogId = (String) params.get("catelogId");
            if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
                wrapper.eq(SpuInfoEntity::getCatalogId, catelogId);
            }
            String key = (String) params.get("key");
            if (!StringUtils.isEmpty(key)) {
                wrapper.and((w) -> w.like(SpuInfoEntity::getSpuName, key).or().like(SpuInfoEntity::getId, key));
            }
            String status = (String) params.get("status");
            if (!StringUtils.isEmpty(status)) {
                wrapper.eq(SpuInfoEntity::getPublishStatus, status);
            }
        }
        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    //TODO:高级部分再优化
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //1.保存spu基本信息:pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        spuInfoDao.insert(spuInfoEntity);
        //2.保存Spu的描述图片:pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescDao.insert(spuInfoDescEntity);
        //3.保存Spu的图片集:pms_spu_images
        List<String> images = vo.getImages();
        if (images != null || images.size() != 0) {
            List<SpuImagesEntity> spuImages = images.stream().map((image) -> {
                SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                spuImagesEntity.setSpuId(spuInfoEntity.getId());
                spuImagesEntity.setImgUrl(image);
                return spuImagesEntity;
            }).toList();
            spuImagesService.saveBatch(spuImages);
        }
        //4.保存Spu的规格参数： pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> valueEntities = baseAttrs.stream().map((baseAttr) -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(baseAttr.getAttrId());
            AttrEntity attr = attrService.getById(baseAttr.getAttrId());
            productAttrValueEntity.setAttrName(attr.getAttrName());
            productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
            productAttrValueEntity.setQuickShow(baseAttr.getShowDesc());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            return productAttrValueEntity;
        }).toList();
        productAttrValueService.saveBatch(valueEntities);
        //5.保存spu的积分信息：gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        spuBoundTo.setBuyBounds(bounds.getBuyBounds());
        spuBoundTo.setGrowBounds(bounds.getGrowBounds());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息不成功");
        }
        //6.保存当前Spu对应的所有sku信息
        //6.1) sku的基本信息:pms_sku_info
        List<Skus> skus = vo.getSkus();
        if (!skus.isEmpty()) {
            skus.forEach((sku) -> {
                String defaultIamge = "";
                for (Images image : sku.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultIamge = image.getImgUrl();
                    }
                }

                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultIamge);
                skuInfoService.save(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> skuImages = sku.getImages().stream().map((image) -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(image.getImgUrl());
                    skuImagesEntity.setDefaultImg(image.getDefaultImg());
                    return skuImagesEntity;
                }).filter((image) -> !StringUtils.isEmpty(image.getImgUrl())).toList();
                //6.2) sku的图片信息:pms_sku_images
                skuImagesService.saveBatch(skuImages);
                //6.3) sku的销售属性信息:pms_sku_sale_attr_value
                List<Attr> attrs = sku.getAttr();
                List<SkuSaleAttrValueEntity> attrSaleValues = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity valueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, valueEntity);
                    valueEntity.setSkuId(skuId);
                    return valueEntity;
                }).toList();
                skuSaleAttrValueService.saveBatch(attrSaleValues);
                //6.4) sku的优惠、满减等信息:gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price\
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                skuReductionTo.setSkuId(skuInfoEntity.getSkuId());
                BeanUtils.copyProperties(sku, skuReductionTo);
                System.out.println(skuReductionTo);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠、满减信息不成功");
                    }
                }

            });
        }

    }

    /**
     * 商品上架功能
     *
     * @param spuId
     */
    @Override
    public void up(Long spuId) {

        //查询所有sku可以被检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(ProductAttrValueEntity::getAttrId).toList();
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        Set<Long> idSet = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream()
                .filter(item -> idSet.contains(item.getAttrId()))
                .map(attr -> {
                    SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(attr, attrs1);
                    return attrs1;
                }).toList();

        //1.查出spuId所有sku信息，品牌的名字、照片
        List<SkuInfoEntity> skuInfos = skuInfoService.getSkuBySpuId(spuId);
        List<Long> skuIds = skuInfos.stream().map(SkuInfoEntity::getSkuId).toList();
        //TODO 1: 发送请求给ware服务查询是否还有库存
        Map<Long, Boolean> skusHashStockMap = null;
        try {
            List<SkuHasStockTo> skusHasStock = wareFeignService.getSkusHasStock(skuIds);
            skusHashStockMap = skusHasStock.stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
        } catch (Exception e) {
            log.error("库存服务查询异常:原因{}", e);
        }
        //2.封装每个sku信息
        Map<Long, Boolean> finalSkusHashStockMap = skusHashStockMap;
        List<SkuEsModel> upProducts = skuInfos.stream().map(skuInfo -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(skuInfo, skuEsModel);
            skuEsModel.setSkuPrice(skuInfo.getPrice());
            skuEsModel.setSkuImg(skuInfo.getSkuDefaultImg());
            //设置库存信息
            if(finalSkusHashStockMap != null){
                skuEsModel.setHasStock(finalSkusHashStockMap.get(skuInfo.getSkuId()));
            }else{
                skuEsModel.setHasStock(true);
            }

            //TODO 2: 热度评分。
            skuEsModel.setHostScore(0L);
            BrandEntity brand = brandDao.selectById(skuInfo.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());
            CategoryEntity category = categoryDao.selectById(skuInfo.getCatalogId());
            if (category != null) {
                skuEsModel.setCatalogName(category.getName());
            }else{
                skuEsModel.setCatalogName("");
            }

            skuEsModel.setAttrs(attrsList);
            return skuEsModel;
        }).toList();
        //3. 数据发送给es进行保存:gulimall-search
        R r = searchFeignService.productStatusUp(upProducts);
        if(r.getCode()==0){
            //远程调用成功
            //修改当前spu的状态
            spuInfoDao.updateSpuStatus(spuId, ProductConstant.StatusEnum.UP_SPU.getCode());
        }else{
            //远程调用失败
            //TODO: 重复调用问题？接口幂等性；重试机制
        }
    }
}