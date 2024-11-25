package com.zxl.gulimall.product;

import com.zxl.gulimall.product.dao.AttrDao;
import com.zxl.gulimall.product.dao.SkuSaleAttrValueDao;
import com.zxl.gulimall.product.service.CategoryService;
import com.zxl.gulimall.product.vo.SkuItemSaleAttrsVo;
import com.zxl.gulimall.product.vo.SkuItemVo;
import com.zxl.gulimall.product.vo.SpuItemAttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Slf4j
class GulimallProductApplicationTests {
    @Resource
    private CategoryService categoryService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private AttrDao attrDao;
    @Resource
    private SkuSaleAttrValueDao skuSaleAttrValueDao;


    @Test
    public void testRedisson() {
        System.out.println(redissonClient);
    }
    @Test
    public void testGetSaleAttrsBySpuId() {
        List<SkuItemSaleAttrsVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrsBySpuId(19L);
        System.out.println(saleAttrsBySpuId.toString());
    }

    @Test
    public void testGetAttrGroupWithAttrsBySpuId() {
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrDao.getAttrGroupWithAttrsBySpuId(19L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId.toString());
    }

    @Test
    public void testFindPath() {
        Long[] catelogPath = categoryService.findCatelogPath(226L);
        log.info("完整路径：{}", Arrays.asList(catelogPath));
    }

    @Test
    void contextLoads() {

    }

}
