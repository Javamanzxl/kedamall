package com.zxl.gulimall.product.controller;

import java.util.Arrays;
import java.util.Map;

import com.zxl.gulimall.product.vo.AttrRespVo;
import com.zxl.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zxl.gulimall.product.entity.AttrEntity;
import com.zxl.gulimall.product.service.AttrService;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.R;

import javax.annotation.Resource;


/**
 * 商品属性
 *
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:38:35
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Resource
    private AttrService attrService;

    /**
     * 列表,如果attrType = 0 是销售属性 1 为普通属性
     */
    @RequestMapping("/{attrType}/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("attrType") String attrType, @PathVariable("catelogId") Long catelogId) {
        PageUtils page = attrService.queryPage(params, attrType, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId) {
//		AttrEntity attr = attrService.getById(attrId);
        AttrRespVo attr = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attr) {
//		attrService.save(attr);
        attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr) {
        attrService.updateCascade(attr);
        return R.ok();
    }


    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds) {
//		attrService.removeByIds(Arrays.asList(attrIds));
        attrService.removeCascade(Arrays.asList(attrIds));
        return R.ok();
    }

}
