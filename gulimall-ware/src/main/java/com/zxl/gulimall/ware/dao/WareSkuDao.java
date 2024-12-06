package com.zxl.gulimall.ware.dao;

import com.zxl.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:41:13
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(WareSkuEntity wareSkuEntity);

    Long getSkuStock(Long skuId);

    List<Long> listWareHasStock(Long skuId);

    Long lockSkuStock(@Param("skuId") Long skuId, @Param("wareId")Long wareId, @Param("num")Integer num);

    void releaseLocked(@Param("skuId")Long skuId, @Param("wareId")Long wareId, @Param("skuNum")Integer skuNum);
}
