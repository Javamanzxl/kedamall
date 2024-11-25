package com.zxl.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @author ：zxl
 * @Description: 检索条件VO
 * @ClassName: SearchParam
 * @date ：2024/11/19 15:41
 */
@Data
public class SearchParam {
    private String keyword; //全文匹配关键字
    private Long catalog3Id; //三级分类id
    /**
     *  sort = saleCount_asc/desc
     *  sort = skuPrice_asc/desc
     *  sort = hostScore_asc/desc
     */
    private String sort; //排序条件
    /**
     * 过滤条件
     *  hasStock(是否有货)、skuPrice区间、brandId、catalog3Id、attrs
     *  hasStock = 0/1
     *  skuPrice = 1_500/_500/500_
     *  brandId=1
     *  attrs=2_五寸:六寸
     */
    private Integer hasStock; //是否只显示有货
    private String skuPrice; //价格区间查询
    private List<Long> brandId; //品牌id,按照品牌查询可以多选
    private List<String> attrs; //按照属性进行筛选
    private Integer pageNum=1; //页码
    private String queryString; //原生的查询条件

}
