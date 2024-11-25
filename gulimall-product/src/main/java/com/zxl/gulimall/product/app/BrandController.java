package com.zxl.gulimall.product.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.zxl.common.to.BrandTo;
import com.zxl.common.valid.AddGroup;
import com.zxl.common.valid.UpdateGroup;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.zxl.gulimall.product.entity.BrandEntity;
import com.zxl.gulimall.product.service.BrandService;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.R;



/**
 * 品牌
 *
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:38:33
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * feign查brand
     * @param brandIds
     * @return
     */
    @GetMapping("/feign/infos")
    public List<BrandTo> infos(@RequestParam List<Long> brandIds){
        List<BrandEntity> brands = brandService.getBrandIds(brandIds);
        List<BrandTo> brandTos = new ArrayList<>();
        BeanUtils.copyProperties(brands,brandTos);
        return brandTos;
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand){
		brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@Validated({UpdateGroup.class}) @RequestBody BrandEntity brand){
//		brandService.updateById(brand);
        brandService.updateCascade(brand);

        return R.ok();
    }

    /**
     * 是否显示
     * @param brandEntity
     * @return
     */
    @PostMapping("/updateStatus")
    public R updateStatus(@RequestBody BrandEntity brandEntity){
        brandService.updateStatus(brandEntity.getBrandId(),brandEntity.getShowStatus());
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }



}
