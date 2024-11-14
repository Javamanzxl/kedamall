package com.zxl.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxl.common.constant.ProductConstant;
import com.zxl.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.zxl.gulimall.product.dao.AttrGroupDao;
import com.zxl.gulimall.product.dao.CategoryDao;
import com.zxl.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.zxl.gulimall.product.entity.AttrGroupEntity;
import com.zxl.gulimall.product.entity.CategoryEntity;
import com.zxl.gulimall.product.service.CategoryService;
import com.zxl.gulimall.product.vo.AttrGroupRelationVo;
import com.zxl.gulimall.product.vo.AttrRespVo;
import com.zxl.gulimall.product.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.Query;

import com.zxl.gulimall.product.dao.AttrDao;
import com.zxl.gulimall.product.entity.AttrEntity;
import com.zxl.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Attr;

import javax.annotation.Resource;
import javax.el.LambdaExpression;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Resource
    private AttrDao attrDao;
    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Resource
    private AttrGroupDao attrGroupDao;
    @Resource
    private CategoryDao categoryDao;
    @Resource
    private CategoryService categoryService;


    @Override
    public PageUtils queryPage(Map<String, Object> params, String attrType, Long catelogId) {
        IPage<AttrEntity> page;
        LambdaQueryWrapper<AttrEntity> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(AttrEntity::getAttrType,
                "base".equalsIgnoreCase(attrType) ?
                        ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() :
                        ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        if (catelogId != 0) {
            wrapper.eq(AttrEntity::getCatelogId, catelogId);

        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.like(AttrEntity::getAttrName, key).or().like(AttrEntity::getCatelogId, key);
        }
        page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        if (!records.isEmpty()) {
            List<AttrRespVo> attrRespVos = records.stream().map((attrEntity -> {
                AttrRespVo attrRespVo = new AttrRespVo();
                BeanUtils.copyProperties(attrEntity, attrRespVo);
                //设置分类和分组的名字
                AttrAttrgroupRelationEntity attrgroupRelation =
                        attrAttrgroupRelationDao.selectOne(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>().eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId()));
                if (attrRespVo.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attrgroupRelation != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupRelation.getAttrGroupId());
                    if (attrGroupEntity != null) {
                        attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                    }
                }
                CategoryEntity category = categoryDao.selectById(attrEntity.getCatelogId());
                if (category != null) {
                    attrRespVo.setCatelogName(category.getName());
                }
                return attrRespVo;
            })).toList();
            pageUtils.setList(attrRespVos);
        }
        return pageUtils;
    }


    /**
     * 保存attr表和attr_group关系表
     *
     * @param attr
     */
    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        attrDao.insert(attrEntity);
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }

    }

    /**
     * 级联删除attr表和attr attrGroup关系表
     *
     * @param list
     */
    @Override
    public void removeCascade(List<Long> list) {
        attrDao.deleteBatchIds(list);
        LambdaQueryWrapper<AttrAttrgroupRelationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AttrAttrgroupRelationEntity::getAttrId, list);
        attrAttrgroupRelationDao.delete(wrapper);
    }

    /**
     * 查询attr信息并附加catelogPath路径信息
     *
     * @param attrId
     * @return
     */
    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrEntity attrEntity = attrDao.selectById(attrId);
        AttrRespVo attrRespVo = new AttrRespVo();
        BeanUtils.copyProperties(attrEntity, attrRespVo);
        //查询catelogPath
        Long[] catelogPath = categoryService.findCatelogPath(attrEntity.getCatelogId());
        attrRespVo.setCatelogPath(catelogPath);
        CategoryEntity category = categoryDao.selectById(attrEntity.getCatelogId());
        attrRespVo.setCatelogName(category.getName());
        //查询分组信息
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            LambdaQueryWrapper<AttrAttrgroupRelationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AttrAttrgroupRelationEntity::getAttrId, attrId);
            AttrAttrgroupRelationEntity attrgroup = attrAttrgroupRelationDao.selectOne(wrapper);
            if (attrgroup != null) {
                attrRespVo.setAttrGroupId(attrgroup.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroup.getAttrGroupId());
                attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
        }
        return attrRespVo;
    }

    /**
     * 级联更新
     *
     * @param attr
     */
    @Override
    public void updateCascade(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        attrDao.updateById(attrEntity);
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //更新relation表
            LambdaQueryWrapper<AttrAttrgroupRelationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId());
            Integer count = attrAttrgroupRelationDao.selectCount(wrapper);
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attr.getAttrId());
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            if (count > 0) {
                attrAttrgroupRelationDao.update(relationEntity, wrapper);
            } else {
                attrAttrgroupRelationDao.insert(relationEntity);
            }
        }

    }

    /**
     * 根据分组id找到关联的所有属性
     *
     * @param attrGroupId
     * @return
     */
    @Override
    public List<AttrEntity> getAttrRelation(Long attrGroupId) {
        LambdaQueryWrapper<AttrAttrgroupRelationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttrAttrgroupRelationEntity::getAttrGroupId, attrGroupId);
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(wrapper);
        List<Long> attrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).toList();
        if (attrIds.isEmpty()) {
            return null;
        }
        return attrDao.selectBatchIds(attrIds);
    }

    /**
     * 删除关联关系
     *
     * @param relationVos
     */
    @Override
    public void deleteRelation(AttrGroupRelationVo[] relationVos) {
        List<AttrAttrgroupRelationEntity> list = Arrays.stream(relationVos).map((item) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).toList();
        attrAttrgroupRelationDao.deleteBatchRelation(list);
    }

    /**
     * 分页查询该分类
     *
     * @param params
     * @param attrGroupId
     * @return
     */
    @Override
    public PageUtils noRelaitonList(Map<String, Object> params, Long attrGroupId) {
        //1.当前分组只能关联自己所属的分类的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //2.当前分组只能关联别的分组没有引用的属性
        //2.1 当前分类下的其他分组
        List<AttrGroupEntity> attrGroups = attrGroupDao.selectList(new LambdaQueryWrapper<AttrGroupEntity>().eq(AttrGroupEntity::getCatelogId, catelogId).ne(AttrGroupEntity::getAttrGroupId, attrGroupId));
        //2.2 其他分组关联的属性
        List<Long> attrGroupIds = attrGroups.stream().map(AttrGroupEntity::getAttrGroupId).toList();
        LambdaQueryWrapper<AttrEntity> wrapper = new LambdaQueryWrapper<>();
        if (!attrGroupIds.isEmpty()) {
            List<AttrAttrgroupRelationEntity> relations = attrAttrgroupRelationDao.selectList(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>().in(AttrAttrgroupRelationEntity::getAttrGroupId, attrGroupIds));
            List<Long> attrIds = relations.stream().map(AttrAttrgroupRelationEntity::getAttrId).toList();
            //2.3 从当前分类的所有属性中移除这些属性
            wrapper.eq(AttrEntity::getCatelogId, catelogId)
                    .eq(AttrEntity::getAttrType, ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
            if (!attrIds.isEmpty()) {
                wrapper.notIn(AttrEntity::getAttrId, attrIds);
            }
            //2.4移除自己已经关联的属性
            List<AttrAttrgroupRelationEntity> thisRelation = attrAttrgroupRelationDao.selectList(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>().eq(AttrAttrgroupRelationEntity::getAttrGroupId, attrGroupId));
            List<Long> thisAttrIds = thisRelation.stream().map(AttrAttrgroupRelationEntity::getAttrId).toList();
            if (!thisAttrIds.isEmpty()) {
                wrapper.notIn(AttrEntity::getAttrId, thisAttrIds);
            }
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.like(AttrEntity::getAttrId, key).or().like(AttrEntity::getAttrName, key);
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        return new PageUtils(page);

    }

    /**
     * 搜索出可以检索的attrId
     * @param attrIds
     * @return
     */
    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
//        LambdaQueryWrapper<AttrEntity> wrapper = new LambdaQueryWrapper<>();
//        wrapper.in(AttrEntity::getAttrId,attrIds);
//        wrapper.eq(AttrEntity::getSearchType,1);
//        List<AttrEntity> attrs = attrDao.selectList(wrapper);
//        return attrs.stream().map(AttrEntity::getAttrId).toList();
        return attrDao.selectSearchAttrIds(attrIds);
    }
}