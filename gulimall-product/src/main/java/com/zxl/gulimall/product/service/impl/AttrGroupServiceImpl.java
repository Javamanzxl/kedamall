package com.zxl.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxl.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.zxl.gulimall.product.dao.AttrDao;
import com.zxl.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.zxl.gulimall.product.entity.AttrEntity;
import com.zxl.gulimall.product.vo.AttrGroupRelationVo;
import com.zxl.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.Query;

import com.zxl.gulimall.product.dao.AttrGroupDao;
import com.zxl.gulimall.product.entity.AttrGroupEntity;
import com.zxl.gulimall.product.service.AttrGroupService;

import javax.annotation.Resource;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private AttrGroupDao attrGroupDao;
    @Resource
    private AttrAttrgroupRelationDao relationDao;
    @Resource
    private AttrDao attrDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCatelogId(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        LambdaQueryWrapper<AttrGroupEntity> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> obj.eq(AttrGroupEntity::getAttrGroupId, key).or()
                    .like(AttrGroupEntity::getAttrGroupName, key));
        }
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        } else {
            wrapper.eq(AttrGroupEntity::getCatelogId, catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }
    }

    /**
     * 获取分类下所有分组&关联属性
     *
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getGroupWithAttr(Long catelogId) {

        //1.查出当前分类下的所有属性分组
        List<AttrGroupEntity> attrGroups = attrGroupDao.selectList(new LambdaQueryWrapper<AttrGroupEntity>()
                .eq(AttrGroupEntity::getCatelogId, catelogId));
        //2.查出每个属性分组的所有属性
        if (!attrGroups.isEmpty()) {
            //取出属性分组的id
            List<Long> attrGroupIds = attrGroups.stream().map(AttrGroupEntity::getAttrGroupId).toList();
            //stream遍历这些ids并返回vos
            List<AttrGroupWithAttrsVo> vos = attrGroupIds.stream().map((attrGroupId) -> {
                //从relation按照attrGroup查询出实体列表
                List<AttrAttrgroupRelationEntity> relationEntities = relationDao.selectList(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq(AttrAttrgroupRelationEntity::getAttrGroupId, attrGroupId));
                if (relationEntities.isEmpty()) {
                    return null; // 直接跳过，没有关联属性的分组
                }
                AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
                List<Long> attrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).toList();
                List<AttrEntity> attrs = attrDao.selectBatchIds(attrIds);
                AttrGroupEntity attrGroup = attrGroupDao.selectById(attrGroupId);
                BeanUtils.copyProperties(attrGroup, vo);
                vo.setAttrs(attrs);
                return vo;
            }).filter(Objects::nonNull).toList();
            return vos;
        }
        return null;
    }
}