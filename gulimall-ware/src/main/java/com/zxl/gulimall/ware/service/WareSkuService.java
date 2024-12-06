package com.zxl.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxl.common.to.OrderTo;
import com.zxl.common.to.SkuHasStockTo;
import com.zxl.common.utils.PageUtils;
import com.zxl.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.zxl.gulimall.ware.entity.WareSkuEntity;
import com.zxl.gulimall.ware.vo.LockStockResultVo;
import com.zxl.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:41:13
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByCondition(Map<String, Object> params);

    void addStock(WareSkuEntity wareSkuEntity);

    List<SkuHasStockTo> getSkusHasStock(List<Long> skuIds);

    boolean hasStockBySkuId(Long skuId);

    Boolean orderLockStock(WareSkuLockVo vo);

    void unlockStock(OrderTo order);
    void unlockStock(WareOrderTaskDetailEntity taskDetailEntity);
}

