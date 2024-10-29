package com.zxl.gulimall.product;

import com.zxl.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;

@SpringBootTest
@Slf4j
class GulimallProductApplicationTests {
    @Resource
    private CategoryService categoryService;

    @Test
    public void testFindPath() {
        Long[] catelogPath = categoryService.findCatelogPath(226L);
        log.info("完整路径：{}", Arrays.asList(catelogPath));
    }

    @Test
    void contextLoads() {

    }

}
