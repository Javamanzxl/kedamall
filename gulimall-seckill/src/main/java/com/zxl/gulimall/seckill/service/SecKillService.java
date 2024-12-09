package com.zxl.gulimall.seckill.service;

import com.zxl.gulimall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

public interface SecKillService {
    void uploadSecKillLatest3Days();

    List<SeckillSkuRedisTo> getCurrentTimeSeckillSkus();

    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
