package com.zxl.gulimall.search.vo;

import com.zxl.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ：zxl
 * @Description: 封装检索返回数据
 * @ClassName: SearchResponse
 * @date ：2024/11/19 16:03
 */
@Data
public class SearchResult {
    private List<SkuEsModel> products; //查询到的所有商品信息
    /**
     * 分页信息
     */
    private Integer pageNum; //当前页码
    private Long total; //总记录数
    private Integer totalPages; //总页码
    private List<Integer> pageNavs; //导航页码

    private List<BrandVo> brands; //当前查询的结果涉及到的品牌信息
    private List<CatalogVo> catalogs; //当前查询的结果涉及到的分类信息
    private List<AttrVo> attrs; //当前查询的结果涉及到的属性信息

    /**
     * 面包屑导航数据
     */
    private List<NavVo> navs = new ArrayList<>();
    private List<Long> attrIds = new ArrayList<>(); //已经选择的attr

    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
    }

    /**
     * 内部类封装品牌信息
     */
    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImage;
    }

    /**
     * 内部类封装属性信息
     */
    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    /**
     * 内部类封装分类信息
     */
    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }
}
