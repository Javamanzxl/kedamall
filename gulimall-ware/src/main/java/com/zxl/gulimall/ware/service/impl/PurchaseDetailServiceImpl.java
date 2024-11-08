package com.zxl.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.Query;

import com.zxl.gulimall.ware.dao.PurchaseDetailDao;
import com.zxl.gulimall.ware.entity.PurchaseDetailEntity;
import com.zxl.gulimall.ware.service.PurchaseDetailService;

import javax.annotation.Resource;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Resource
    private PurchaseDetailDao purchaseDetailDao;
    /***
     * 分页条件查询
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseDetailEntity> wrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");
        String status = (String) params.get("status");
        String wareId = (String) params.get("wareId");
        if(!StringUtils.isEmpty(key)){
            wrapper.like(PurchaseDetailEntity::getSkuId,key)
                    .or().like(PurchaseDetailEntity::getPurchaseId,key);
        }
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq(PurchaseDetailEntity::getStatus,status);
        }
        if(!StringUtils.isEmpty(wareId)){
            wrapper.eq(PurchaseDetailEntity::getWareId,wareId);
        }
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                new QueryWrapper<PurchaseDetailEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据采购单id查询
     * @param purchaseId
     * @return
     */
    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long purchaseId) {
        LambdaQueryWrapper<PurchaseDetailEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseDetailEntity::getPurchaseId,purchaseId);
        return purchaseDetailDao.selectList(wrapper);
    }
}