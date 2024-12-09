package com.zxl.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zxl.common.constant.SeckillConstant;
import com.zxl.common.to.MemberTo;
import com.zxl.common.to.mq.SeckillOrderTo;
import com.zxl.common.utils.R;
import com.zxl.gulimall.seckill.feign.CouponFeignService;
import com.zxl.gulimall.seckill.feign.ProductFeignService;
import com.zxl.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.zxl.gulimall.seckill.service.SecKillService;
import com.zxl.gulimall.seckill.to.SeckillSkuRedisTo;
import com.zxl.gulimall.seckill.vo.SeckillSessionVo;
import com.zxl.gulimall.seckill.vo.SeckillSkuRelationVo;
import com.zxl.gulimall.seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: SecKillServiceImpl
 * @date ：2024/12/07 18:13
 */
@Service
public class SecKillServiceImpl implements SecKillService {
    @Resource
    private CouponFeignService couponFeignService;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 获取某一个商品的秒杀信息
     *
     * @param skuId
     * @return
     */

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        //1.找到所有需要参与秒杀的key
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SeckillConstant.SKUKILL_CACHE_PREFIX);
        Set<String> keys = ops.keys();
        if (keys != null && !keys.isEmpty()) {
            String reg = "\\d_" + skuId;
            for (String key : keys) {
                if (Pattern.matches(reg, key)) {
                    String json = ops.get(key);
                    SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    //处理随机码
                    long startTime = seckillSkuRedisTo.getStartTime();
                    long endTime = seckillSkuRedisTo.getEndTime();
                    long curTime = new Date().getTime();
                    if (curTime >= startTime && curTime <= endTime) {

                    } else {
                        seckillSkuRedisTo.setRandomCode(null);
                    }
                    return seckillSkuRedisTo;
                }
            }
        }
        return null;
    }

    /**
     * 返回当前时间参与的秒杀商品信息
     *
     * @return
     */
    @Override
    public List<SeckillSkuRedisTo> getCurrentTimeSeckillSkus() {
        //1.确定当前时间属于哪个秒杀场次
        long curTime = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SeckillConstant.SESSIONS_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                String replace = key.replace(SeckillConstant.SESSIONS_CACHE_PREFIX, "");
                String[] s = replace.split("_");
                long startTime = Long.parseLong(s[0]);
                long endTime = Long.parseLong(s[1]);
                if (curTime >= startTime && curTime <= endTime) {
                    //2.获取这个秒杀场次需要的所有商品信息
                    List<String> sessionSkuIds = redisTemplate.opsForList().range(key, -100, 100);
                    BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SeckillConstant.SKUKILL_CACHE_PREFIX);
                    List<String> list = ops.multiGet(sessionSkuIds);
                    if (list != null) {
                        return list.stream()
                                .map(item -> JSON.parseObject(item, SeckillSkuRedisTo.class))
                                .toList();
                    }
                    break;
                }
            }

        }


        return null;
    }

    @Override
    public void uploadSecKillLatest3Days() {
        //1.去扫描最近三天需要参与秒杀的活动
        R r = couponFeignService.getLatest3DaySession();
        if (r.getCode() == 0) {
            List<SeckillSessionVo> sessions = r.getData(new TypeReference<List<SeckillSessionVo>>() {
            });
            //缓存到redis中
            //1.缓存活动信息
            saveSessionInfos(sessions);
            //2.缓存活动关联的商品信息
            saveSessionSkuInfos(sessions);
        }
    }

    /**
     * 缓存活动信息
     *
     * @param sessions
     */
    private void saveSessionInfos(List<SeckillSessionVo> sessions) {
        sessions.forEach(session -> {
            //long类型为时间戳
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SeckillConstant.SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
            Boolean hasKey = redisTemplate.hasKey(key);
            if (Boolean.FALSE.equals(hasKey)) {
                List<String> sessionSkuIds = session.getRelationSkus().stream()
                        .map(relation -> relation.getPromotionSessionId() + "_" + relation.getSkuId())
                        .toList();
                redisTemplate.opsForList().leftPushAll(key, sessionSkuIds);
            }
        });
    }

    /**
     * 缓存活动关联的商品信息
     *
     * @param sessions
     */
    private void saveSessionSkuInfos(List<SeckillSessionVo> sessions) {
        sessions.forEach(session -> {
            //准备hash操作
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SeckillConstant.SKUKILL_CACHE_PREFIX);
            List<SeckillSkuRelationVo> relations = session.getRelationSkus();
            relations.forEach(relation -> {
                //生产商品随机码
                String randomCode = UUID.randomUUID().toString().replace("-", "");
                String key = relation.getPromotionSessionId() + "_" + relation.getSkuId();
                Boolean has = ops.hasKey(key);
                if (Boolean.FALSE.equals(has)) {
                    SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();
                    SkuInfoVo skuInfoVo = productFeignService.infoBySkuId(relation.getSkuId());
                    //封装sku秒杀信息
                    BeanUtils.copyProperties(relation, seckillSkuRedisTo);
                    //封装活动开始和结束时间
                    seckillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                    seckillSkuRedisTo.setEndTime(session.getEndTime().getTime());
                    //封装sku基本信息
                    seckillSkuRedisTo.setSkuInfoVo(skuInfoVo);
                    //封装商品的随机码
                    seckillSkuRedisTo.setRandomCode(randomCode);
                    ops.put(key, JSON.toJSONString(seckillSkuRedisTo));
                    //如果当前这个场次的商品的库存信息已经上架就不需要上架了
                    //设置秒杀商品分布式信息量作为库存扣减信息
                    RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + randomCode);
                    semaphore.trySetPermits(relation.getSeckillCount().intValue());
                }
            });
        });
    }

    /**
     * 秒杀功能
     *
     * @param killId
     * @param key
     * @param num
     * @return
     */
    @Override
    public String kill(String killId, String key, Integer num) {
        //1.获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SeckillConstant.SKUKILL_CACHE_PREFIX);
        String json = ops.get(killId);
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        SeckillSkuRedisTo to = JSON.parseObject(json, SeckillSkuRedisTo.class);
        //2.校验合法性
        //2.1校验时间
        Long startTime = to.getStartTime();
        Long endTime = to.getEndTime();
        long nowTime = new Date().getTime();
        if (nowTime <= endTime && nowTime >= startTime) {
            //2.2校验随机码和商品id
            String randomCode = to.getRandomCode();
            String skuId = to.getPromotionSessionId() + "_" + to.getSkuId();
            if (randomCode.equals(key) && killId.equals(skuId)) {
                //2.3验证购物数量是否合理
                BigDecimal seckillLimit = to.getSeckillLimit();
                if (num <= seckillLimit.intValue()) {
                    //2.4验证这个人是否已经买过了。幂等性。
                    //如果秒杀成功，就去redis占位。 key:userId_sessionId_skuId;
                    MemberTo member = LoginUserInterceptor.loginUser.get();
                    String userKey = member.getId() + "_" + to.getPromotionSessionId() + "_" + skuId;
                    long ttl = endTime - nowTime;
                    Boolean b = redisTemplate.opsForValue().setIfAbsent(userKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                    if (Boolean.TRUE.equals(b)) {
                        //如果占位成功说明没有秒杀过
                        //2.5获取信号量
                        RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + randomCode);
                        try {
                            //拿到信号量秒杀成功
                            boolean b1 = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                            if (b1) {
                                //2.6快速下单，发送MQ消息
                                String orderSn = IdWorker.getTimeId();
                                SeckillOrderTo seckillOrder = new SeckillOrderTo();
                                seckillOrder.setOrderSn(orderSn);
                                seckillOrder.setMemberId(member.getId());
                                seckillOrder.setNum(num);
                                seckillOrder.setPromotionSessionId(to.getPromotionSessionId());
                                seckillOrder.setSeckillPrice(to.getSeckillPrice());
                                seckillOrder.setSkuId(to.getSkuId());
                                rabbitTemplate.convertAndSend("order-event-exchange"
                                        , "order.seckill.order"
                                        , seckillOrder);
                                return orderSn;
                            }
                            return null;
                        } catch (InterruptedException e) {
                            return null;
                        }
                    } else {
                        return null;
                    }

                } else {
                    return null;
                }
            } else {
                return null;
            }

        } else {
            return null;
        }
    }
}
