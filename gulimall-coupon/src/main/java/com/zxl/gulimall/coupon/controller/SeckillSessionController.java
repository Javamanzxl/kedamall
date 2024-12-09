package com.zxl.gulimall.coupon.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zxl.gulimall.coupon.entity.SeckillSessionEntity;
import com.zxl.gulimall.coupon.service.SeckillSessionService;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.R;

import javax.annotation.Resource;


/**
 * 秒杀活动场次
 *
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:39:33
 */
@RestController
@RequestMapping("coupon/seckillsession")
public class SeckillSessionController {
    @Resource
    private SeckillSessionService seckillSessionService;

    /**
     * 获取最近三天的秒杀活动
     * @return
     */
    @GetMapping("/latest3DaySession")
    public R getLatest3DaySession(){
        List<SeckillSessionEntity> seckillSessions =  seckillSessionService.getLatest3DaySession();
        return R.ok().setData(seckillSessions);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = seckillSessionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		SeckillSessionEntity seckillSession = seckillSessionService.getById(id);

        return R.ok().put("seckillSession", seckillSession);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SeckillSessionEntity seckillSession) throws ParseException {
        seckillSessionService.save(seckillSession);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SeckillSessionEntity seckillSession){
		seckillSessionService.updateById(seckillSession);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		seckillSessionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
