package com.zxl.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.zxl.gulimall.product.entity.AttrEntity;
import com.zxl.gulimall.product.service.*;
import com.zxl.gulimall.product.vo.AttrGroupRelationVo;
import com.zxl.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.web.bind.annotation.*;

import com.zxl.gulimall.product.entity.AttrGroupEntity;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.R;

import javax.annotation.Resource;


/**
 * 属性分组
 *
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:38:34
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Resource
    private AttrGroupService attrGroupService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private AttrService attrService;
    @Resource
    private AttrAttrgroupRelationService relationService;

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId) {
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPageByCatelogId(params, catelogId);
        return R.ok().put("page", page);
    }

    /**
     * 查询属性分组id关联属性
     *
     * @param attrGroupId
     * @return
     */
    @GetMapping("{attrGroupId}/attr/relation")
    public R attrRelation(@PathVariable Long attrGroupId) {
        List<AttrEntity> attrEntities = attrService.getAttrRelation(attrGroupId);
        return R.ok().put("data", attrEntities);
    }

    /**
     * 删除关联关系
     *
     * @param
     * @return
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] relationVos) {
        attrService.deleteRelation(relationVos);
        return R.ok();
    }

    /**
     * 获取分类下所有分组&关联属性
     * @param catelogId
     * @return
     */
    @RequestMapping("/{catelogId}/withattr")
    public R groupWithAttr(@PathVariable(value = "catelogId") Long catelogId){
        //1.查出当前分类下的所有属性分组
        //2.查出每个属性分组的所有属性
        List<AttrGroupWithAttrsVo> vos = attrGroupService.getGroupWithAttr(catelogId);
        return R.ok().put("data",vos);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    /**
     * 分页查询自己没有关联的属性
     * @param params
     * @param attrGroupId
     * @return
     */
    @GetMapping("/{attrGroupId}/noattr/relation")
    public R noRelation(@RequestParam Map<String, Object> params, @PathVariable Long attrGroupId) {
        PageUtils page = attrService.noRelaitonList(params,attrGroupId);
        return R.ok().put("page",page);
    }

    /**
     * 新增关系
     * @param relationVos
     * @return
     */
    @PostMapping("/attr/relation")
    public R addAttrRelation(@RequestBody AttrGroupRelationVo[] relationVos){
        relationService.addAttrRelaiton(relationVos);
        return R.ok();
    }

}
