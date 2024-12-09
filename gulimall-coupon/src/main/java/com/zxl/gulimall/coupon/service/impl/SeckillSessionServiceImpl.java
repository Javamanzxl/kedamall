package com.zxl.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxl.gulimall.coupon.dao.SeckillSkuRelationDao;
import com.zxl.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.zxl.gulimall.coupon.service.SeckillSkuRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.Query;

import com.zxl.gulimall.coupon.dao.SeckillSessionDao;
import com.zxl.gulimall.coupon.entity.SeckillSessionEntity;
import com.zxl.gulimall.coupon.service.SeckillSessionService;

import javax.annotation.Resource;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Resource
    private SeckillSessionDao seckillSessionDao;
    @Resource
    private SeckillSkuRelationService seckillSkuRelationService;
    @Resource
    private SeckillSkuRelationDao seckillSkuRelationDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        LambdaQueryWrapper<SeckillSessionEntity> wrapper = new LambdaQueryWrapper<>();
        String key =(String) params.get("key");
        if(!StringUtils.isBlank(key)){
            wrapper.like(SeckillSessionEntity::getName,key).or()
                    .like(SeckillSessionEntity::getId,key);
        }
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 获取最近三天的秒杀活动
     * @return
     */
    @Override
    public List<SeckillSessionEntity> getLatest3DaySession() {
        //计算最近三天的时间
        //LocalDate格式 yyyy-MM-dd
        LocalDate now = LocalDate.now();
        LocalDate plus = now.plusDays(2);
        //hh:mm:ss
        LocalTime min = LocalTime.MIN;
        LocalTime max = LocalTime.MAX;
        //yyyy-MM-dd HH:mm:ss
        LocalDateTime start = LocalDateTime.of(now, min);
        LocalDateTime end = LocalDateTime.of(plus, max);
        //格式化
        String formatStart = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss"));
        String formatEnd = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss"));
        LambdaQueryWrapper<SeckillSessionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(SeckillSessionEntity::getStartTime,formatStart,formatEnd);

        List<SeckillSessionEntity> seckillSessions = seckillSessionDao.selectList(wrapper);
        if(seckillSessions!=null && !seckillSessions.isEmpty()){
            return seckillSessions.stream().map(session -> {
                Long sessionId = session.getId();
                List<SeckillSkuRelationEntity> relations = seckillSkuRelationDao.selectList(new LambdaQueryWrapper<SeckillSkuRelationEntity>()
                        .eq(SeckillSkuRelationEntity::getPromotionSessionId, sessionId));
                if (relations != null && !relations.isEmpty()) {
                    session.setRelationSkus(relations);
                }
                return session;
            }).toList();
        }
        return null;
    }
}