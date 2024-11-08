package com.zxl.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxl.common.to.SkuInfoTo;
import com.zxl.common.utils.R;
import com.zxl.gulimall.ware.feign.ProductFeign;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.Query;

import com.zxl.gulimall.ware.dao.WareSkuDao;
import com.zxl.gulimall.ware.entity.WareSkuEntity;
import com.zxl.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    private WareSkuDao wareSkuDao;
    @Resource
    private ProductFeign productFeign;

    /**
     * 条件分页查询
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<WareSkuEntity> wrapper = new LambdaQueryWrapper<>();
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq(WareSkuEntity::getSkuId, skuId);
        }
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq(WareSkuEntity::getWareId, wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 完成采购添加库存
     *
     * @param wareSkuEntity
     */
    @Transactional
    @Override
    public void addStock(WareSkuEntity wareSkuEntity) {
        //判断如果没有库存记录就是新增操作
        LambdaQueryWrapper<WareSkuEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WareSkuEntity::getSkuId, wareSkuEntity.getSkuId());
        wrapper.eq(WareSkuEntity::getWareId, wareSkuEntity.getWareId());
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(wrapper);
        if (wareSkuEntities == null || wareSkuEntities.isEmpty()) {
            wareSkuEntity.setStockLocked(0);
            //远程查询sku的名字
            try {
                SkuInfoTo skuInfoTo = productFeign.infoBySkuId(wareSkuEntity.getSkuId());
                if (skuInfoTo != null) {
                    wareSkuEntity.setSkuName(skuInfoTo.getSkuName());
                }
            } catch (Exception e) {

            }
            wareSkuDao.insert(wareSkuEntity);
        } else {
            wareSkuDao.addStock(wareSkuEntity);
        }

    }
}