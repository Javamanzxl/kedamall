package com.zxl.gulimall.product.web;

import com.zxl.gulimall.product.entity.CategoryEntity;
import com.zxl.gulimall.product.service.CategoryService;
import com.zxl.gulimall.product.vo.Catalog2Vo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: IndexController
 * @date ：2024/11/14 20:59
 */
@Controller
public class IndexController {
    @Resource
    private CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {


        //TODO 1、查出所有一级分类
        List<CategoryEntity> categories = categoryService.getLevel1Categorys();
        /**
         *  thymeleaf默认配置了前缀和后缀
         *  prefix:
         *  suffix:   .html
         *  视图解析器会进行拼串
         *      classpath:/templates/ + 返回值(index) + .html
         */
        model.addAttribute("categories", categories);
        return "index";
    }

    //index/catalog.json
    @ResponseBody
    @GetMapping("index/catalog.json")
    public Map<String,List<Catalog2Vo>> getCatalogJson() {
        return categoryService.getCatalogJson();
    }

}
