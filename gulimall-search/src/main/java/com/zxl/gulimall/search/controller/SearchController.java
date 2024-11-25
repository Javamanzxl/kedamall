package com.zxl.gulimall.search.controller;

import com.zxl.gulimall.search.service.MallSearchService;
import com.zxl.gulimall.search.vo.SearchParam;
import com.zxl.gulimall.search.vo.SearchResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author ：zxl
 * @Description: 页面跳转
 * @ClassName: SearchController
 * @date ：2024/11/19 12:13
 */
@Controller
public class SearchController {
    @Resource
    private MallSearchService mallSearchService;

    /**
     * 根据传递来的页面查询参数，去ES中检索商品
     * @param param
     * @return
     */
    @GetMapping("list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request){
        String queryString = request.getQueryString();
        param.setQueryString(queryString);
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("searchResult",result);
        return "list";
    }
}
