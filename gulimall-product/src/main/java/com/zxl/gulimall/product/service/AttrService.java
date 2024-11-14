package com.zxl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxl.common.utils.PageUtils;
import com.zxl.gulimall.product.entity.AttrEntity;
import com.zxl.gulimall.product.vo.AttrGroupRelationVo;
import com.zxl.gulimall.product.vo.AttrRespVo;
import com.zxl.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:38:35
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params,String attrType, Long catelogId);

    void saveAttr(AttrVo attr);

    void removeCascade(List<Long> list);

    AttrRespVo getAttrInfo(Long attrId);

    void updateCascade(AttrVo attr);

    List<AttrEntity> getAttrRelation(Long attrGroupId);

    void deleteRelation(AttrGroupRelationVo[] relationVos);

    PageUtils noRelaitonList(Map<String, Object> params, Long attrGroupId);

    List<Long> selectSearchAttrIds(List<Long> attrIds);
}

