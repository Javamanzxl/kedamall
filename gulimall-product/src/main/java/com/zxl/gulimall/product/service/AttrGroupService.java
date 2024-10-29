package com.zxl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxl.common.utils.PageUtils;
import com.zxl.gulimall.product.entity.AttrGroupEntity;

import java.util.Map;

/**
 * 属性分组
 *
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:38:34
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByCatelogId(Map<String, Object> params, Long catelogId);
}

