package com.zxl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxl.common.utils.PageUtils;
import com.zxl.gulimall.product.entity.AttrEntity;
import com.zxl.gulimall.product.vo.AttrVo;

import java.util.Map;

/**
 * 商品属性
 *
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:38:35
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);
}

