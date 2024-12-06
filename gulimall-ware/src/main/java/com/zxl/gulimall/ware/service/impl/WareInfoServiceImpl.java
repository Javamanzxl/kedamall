package com.zxl.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxl.common.utils.R;
import com.zxl.gulimall.ware.feign.MemberFeignService;
import com.zxl.gulimall.ware.vo.FareVo;
import com.zxl.gulimall.ware.vo.MemberAddressVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.Query;

import com.zxl.gulimall.ware.dao.WareInfoDao;
import com.zxl.gulimall.ware.entity.WareInfoEntity;
import com.zxl.gulimall.ware.service.WareInfoService;

import javax.annotation.Resource;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Resource
    private WareInfoDao wareInfoDao;
    @Resource
    private MemberFeignService memberFeignService;
    /**
     * 条件分页查询
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<WareInfoEntity> wrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)) {
            wrapper.like(WareInfoEntity::getAddress, key).or().like(WareInfoEntity::getName, key)
                    .or().like(WareInfoEntity::getId, key);
        }
        IPage<WareInfoEntity> page = this.page(new Query<WareInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                new QueryWrapper<WareInfoEntity>()
        );
        return new PageUtils(page);
    }

    /**
     * 根据用户地址计算运费
     * @param attrId
     * @return
     */
    @Override
    public FareVo getFare(Long attrId) {
        FareVo fareVo = new FareVo();
        R r = memberFeignService.info(attrId);
        MemberAddressVo address = r.getData2("memberReceiveAddress",new TypeReference<MemberAddressVo>() {});
        fareVo.setAddress(address);
        if(address!=null){
            //该地方模拟运费计算，如果要计算可以找快递公司的计算运费api
            String phone = address.getPhone();
            String fare = phone.substring(phone.length() - 1);
            BigDecimal b = new BigDecimal(fare);
            fareVo.setFare(b);
            return fareVo;
        }
        return null;
    }
}