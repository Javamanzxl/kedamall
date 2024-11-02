package com.zxl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxl.common.utils.PageUtils;
import com.zxl.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.zxl.gulimall.product.vo.AttrGroupRelationVo;

import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:38:34
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addAttrRelaiton(AttrGroupRelationVo[] relationVos);
}

