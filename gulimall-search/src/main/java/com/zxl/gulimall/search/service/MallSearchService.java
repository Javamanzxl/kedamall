package com.zxl.gulimall.search.service;

import com.zxl.gulimall.search.vo.SearchParam;
import com.zxl.gulimall.search.vo.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam param);
}
