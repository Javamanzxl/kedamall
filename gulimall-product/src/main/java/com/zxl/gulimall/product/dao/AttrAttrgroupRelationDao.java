package com.zxl.gulimall.product.dao;

import com.zxl.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxl.gulimall.product.vo.AttrGroupRelationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:38:34
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    void deleteBatchRelation(@Param("list") List<AttrAttrgroupRelationEntity> list);

    void insertBatch(@Param("relations") List<AttrAttrgroupRelationEntity> relations);
}
