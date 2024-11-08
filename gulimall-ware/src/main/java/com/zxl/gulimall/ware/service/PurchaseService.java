package com.zxl.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxl.common.utils.PageUtils;
import com.zxl.gulimall.ware.entity.PurchaseEntity;
import com.zxl.gulimall.ware.vo.MergeVo;
import com.zxl.gulimall.ware.vo.PurchaseFinishVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:41:12
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

    void merge(MergeVo mergeVo);

    void received(List<Long> ids);

    void deleteByIds(List<Long> ids);

    void finish(PurchaseFinishVo finishVo);
}

