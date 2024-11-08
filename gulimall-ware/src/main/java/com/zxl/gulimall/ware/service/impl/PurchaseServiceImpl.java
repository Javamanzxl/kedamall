package com.zxl.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxl.common.constant.WareConstant;
import com.zxl.gulimall.ware.dao.PurchaseDetailDao;
import com.zxl.gulimall.ware.entity.PurchaseDetailEntity;
import com.zxl.gulimall.ware.entity.WareSkuEntity;
import com.zxl.gulimall.ware.service.PurchaseDetailService;
import com.zxl.gulimall.ware.service.WareSkuService;
import com.zxl.gulimall.ware.vo.MergeVo;
import com.zxl.gulimall.ware.vo.PurchaseFinishVo;
import com.zxl.gulimall.ware.vo.PurchaseItemFinishVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.Query;

import com.zxl.gulimall.ware.dao.PurchaseDao;
import com.zxl.gulimall.ware.entity.PurchaseEntity;
import com.zxl.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Resource
    private PurchaseDao purchaseDao;
    @Resource
    private PurchaseDetailService purchaseDetailService;
    @Resource
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 分页查询未领取的订单
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseEntity::getStatus, 0).or().eq(PurchaseEntity::getStatus, 1);
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 合并采购需求到采购单
     *
     * @param mergeVo
     */
    @Transactional
    @Override
    public void merge(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();

        if (purchaseId == null) {
            //新建采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setPriority(1);
            purchaseDao.insert(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        PurchaseEntity purchase = purchaseDao.selectById(purchaseId);
        if (purchase.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() || purchase.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
            //合并到已有的采购单
            List<Long> items = mergeVo.getItems();
            Long finalPurchaseId = purchaseId;
            List<PurchaseDetailEntity> purchaseDetails = items.stream().map(item -> {
                PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
                purchaseDetail.setId(item);
                purchaseDetail.setPurchaseId(finalPurchaseId);
                purchaseDetail.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
                return purchaseDetail;
            }).toList();
            purchaseDetailService.updateBatchById(purchaseDetails);
        }
    }

    @Override
    public void received(List<Long> ids) {
        //1.确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> purchases = ids.stream().map(id -> purchaseDao.selectById(id))
                .filter(purchase -> purchase.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                        purchase.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())
                .map(purchase -> {
                    purchase.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
                    purchase.setUpdateTime(new Date());
                    return purchase;
                }).toList();
        //2.改变采购单的状态
        this.updateBatchById(purchases);
        //3.改变采购单采购项的状态
        purchases.forEach(purchase -> {
            List<PurchaseDetailEntity> detailEntities = purchaseDetailService.listDetailByPurchaseId(purchase.getId());
            List<PurchaseDetailEntity> purchaseDetails = detailEntities.stream().map(detail -> {
                PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
                purchaseDetail.setId(detail.getId());
                purchaseDetail.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return purchaseDetail;
            }).toList();
            purchaseDetailService.updateBatchById(purchaseDetails);
        });
    }

    /**
     * 删除采购单，并把采购项的状态改为采购失败
     *
     * @param ids
     */
    @Override
    public void deleteByIds(List<Long> ids) {
        purchaseDao.deleteBatchIds(ids);
        ids.forEach(id -> {
            List<PurchaseDetailEntity> detailEntities = purchaseDetailService.listDetailByPurchaseId(id);
            List<PurchaseDetailEntity> purchaseDetailEntities = detailEntities.stream().map(detail -> {
                PurchaseDetailEntity newPurchaseDetail = new PurchaseDetailEntity();
                newPurchaseDetail.setId(detail.getId());
                newPurchaseDetail.setStatus(WareConstant.PurchaseDetailStatusEnum.HAS_ERROR.getCode());
                return newPurchaseDetail;
            }).toList();
            purchaseDetailService.updateBatchById(purchaseDetailEntities);
        });
    }

    @Override
    public void finish(PurchaseFinishVo finishVo) {
        //1.改变采购项状态
        boolean flag = true;
        List<PurchaseItemFinishVo> items = finishVo.getItems();
        ArrayList<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemFinishVo item : items) {
            PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HAS_ERROR.getCode()) {
                flag = false;
                purchaseDetail.setStatus(WareConstant.PurchaseDetailStatusEnum.HAS_ERROR.getCode());
            } else {
                purchaseDetail.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                //3.将成功采购的进行入库
                PurchaseDetailEntity detailEntity = purchaseDetailService.getById(item.getItemId());
                WareSkuEntity wareSkuEntity = new WareSkuEntity();
                wareSkuEntity.setSkuId(detailEntity.getSkuId());
                wareSkuEntity.setWareId(detailEntity.getWareId());
                wareSkuEntity.setStock(detailEntity.getSkuNum());
                wareSkuService.addStock(wareSkuEntity);
            }
            purchaseDetail.setId(item.getItemId());
            updates.add(purchaseDetail);
        }
        purchaseDetailService.updateBatchById(updates);
        //2.改变采购单状态
        Long purchaseId = finishVo.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HAS_ERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        purchaseDao.updateById(purchaseEntity);

    }
}