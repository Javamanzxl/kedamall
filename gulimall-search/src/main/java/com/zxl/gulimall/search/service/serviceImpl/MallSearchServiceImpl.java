package com.zxl.gulimall.search.service.serviceImpl;

import com.alibaba.fastjson2.JSON;
import com.zxl.common.to.AttrRespTo;
import com.zxl.common.to.BrandTo;
import com.zxl.common.to.es.SkuEsModel;
import com.zxl.gulimall.search.config.GulimallElasticSearchConfig;
import com.zxl.gulimall.search.constant.EsConstant;
import com.zxl.gulimall.search.feign.ProductFeignService;
import com.zxl.gulimall.search.service.MallSearchService;
import com.zxl.gulimall.search.vo.SearchParam;
import com.zxl.gulimall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: MallSearchServiceImpl
 * @date ：2024/11/19 15:43
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Resource
    private RestHighLevelClient client;
    @Resource
    private ProductFeignService productFeignService;

    /**
     * 检索
     *
     * @param param 检索参数
     * @return 检索结果
     */
    @Override
    public SearchResult search(SearchParam param) {
        SearchResult result = null;
        SearchRequest searchRequest = buildSearchRequest(param);
        try {
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 构建检索请求
     * 模糊匹配，过滤(按照属性、分类、品牌、价格区间、库存)，排序，分页，高亮，聚合分析
     *
     * @return
     */
    //TODO: 认真看DSL语句
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder builder = new SearchSourceBuilder(); //构建DSL语句
        /**
         * 查询：模糊匹配，过滤(按照属性、分类、品牌、价格区间、库存)
         */
        //1.构建bool-query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1 must-模糊匹配
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        //1.2 filter-过滤---按照三级分类id查询
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        //1.2 filter-过滤---按照brandId查询
        if (param.getBrandId() != null && !param.getBrandId().isEmpty()) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        //1.2 filter-过滤---按照属性查询
        if (param.getAttrs() != null && !param.getAttrs().isEmpty()) {
            for (String attrStr : param.getAttrs()) {
                // &attr=1_5寸:8寸
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                //每一个属性都要生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        //1.2 filter-过滤---按照是否有库存查询
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        //1.2 filter-过滤---按照价格区间
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            //  1_500/_500/500_
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                //区间 1_500
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(s[0]);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
        builder.query(boolQuery);
        /**
         * 排序，分页，高亮，
         */
        //2.1排序
        if (!StringUtils.isEmpty(param.getSort())) {
            //sort=hostScore_asc/desc
            String[] s = param.getSort().split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            builder.sort(s[0], order);
        }
        //2.2分页 pageSize:5
        // pageNum:1 from:0 size:5 [0,1,2,3,4]
        // pageNum:2 from:5 size:5
        // from = (pageNum-1)*pageSize
        builder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        builder.size(EsConstant.PRODUCT_PAGESIZE);
        //2.3高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            builder.highlighter(highlightBuilder);
        }
        /**
         *  聚合分析
         */
        //1.品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId").size(50);
        //1.1品牌聚合子聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        builder.aggregation(brandAgg);
        //2.分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        builder.aggregation(catalogAgg);
        //3.属性聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        //3.1聚合出所有attrId
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50);
        //3.1.1聚合出当前attr_id对应的名字
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        //3.1.2聚合出当前attr_id对应的值
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(10));
        attrAgg.subAggregation(attrIdAgg);
        builder.aggregation(attrAgg);
        String string = builder.toString();
        System.out.println("构建dsl" + string);
        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, builder);
    }

    /**
     * 构建结果数据
     *
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();
        //1.封装查询到的商品
        SearchHits hits = response.getHits();
        List<SkuEsModel> skuEsModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                //封装高亮信息
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String s = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(s);
                }
                skuEsModels.add(esModel);
            }
        }
        result.setProducts(skuEsModels);
        /*               以下从聚合信息中获取                 */
        Aggregations aggregations = response.getAggregations();
        //2.封装当前商品所有涉及到的属性信息
        List<SearchResult.AttrVo> attrs = new ArrayList<>();
        ParsedNested attrAgg = aggregations.get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //2.1得到属性id
            Long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);
            //2.2得到属性名字
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);
            //2.3得到属性的值
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attrValueAgg.getBuckets().stream().map(item -> item.getKeyAsString()).toList();
            attrVo.setAttrValue(attrValues);
            attrs.add(attrVo);
        }
        result.setAttrs(attrs);
        //3.封装当前商品所涉及的品牌信息
        ParsedLongTerms brandAgg = aggregations.get("brand_agg");
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            //3.1得到品牌id
            Long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);
            //3.2得到品牌name
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            //3.3得到品牌image
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
            if (brandImgAgg.getSumOfOtherDocCounts() != 0) {
                String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
                brandVo.setBrandImage(brandImg);
            }
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);
        //4.封装当前商品所涉及的分类信息
        ParsedLongTerms catalogAgg = aggregations.get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //4.1得到分类id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            //4.2得到分类名
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);
        /*             以上从聚合信息中获取                 */
        //5.封装分页信息
        //5.1页码
        result.setPageNum(param.getPageNum());
        //5.2总记录数
        Long total = hits.getTotalHits().value;
        result.setTotal(total);
        //5.3总页码—计算得到
        int totalPages = (int) (total % EsConstant.PRODUCT_PAGESIZE) == 0 ? (int) (total / EsConstant.PRODUCT_PAGESIZE) : (int) (total / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages(totalPages);
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);
        //6.构建面包屑导航功能
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                //6.1分析每个attr传过来的查询参数
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                AttrRespTo attrTo = productFeignService.feign_info(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                if (attrTo != null) {
                    navVo.setNavName(attrTo.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }
                navVo.setNavValue(s[1]);
                //6.2取消了面包屑之后，要讲请求中的url里面的当前条件设置为空
                //拿到所有查询条件，去掉当前。
                String replace = replaceQueryString(param, attr,"attrs");
                navVo.setLink("http://search.zxl1027.com/list.html?" + replace);
                return navVo;
            }).toList();
            result.setNavs(navVos);
        }
        //品牌分类面包屑导航
        if(param.getBrandId()!=null && param.getBrandId().size()>0){
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            //远程查询所有品牌
            List<BrandTo> brands = productFeignService.infos(param.getBrandId());
            StringBuffer buffer = new StringBuffer();
            String replace = "";
            for (BrandTo brand : brands) {
                buffer.append(brand.getName()+";");
                replace = replaceQueryString(param, String.valueOf(brand.getBrandId()),"brandId");
            }
            navVo.setNavValue(buffer.toString());
            navVo.setLink("http://search.zxl1027.com/list.html"+replace);
            navs.add(navVo);
        }

        //TODO:分类没做

        return result;
    }

    private String replaceQueryString(SearchParam param, String value, String key) {
        String encode = URLEncoder.encode(value, StandardCharsets.UTF_8);
        //浏览器对空格编码的处理和java不一样
        encode = encode.replace("+","%20");
        String replace = param.getQueryString().replace("&" + key + "=" + encode, "");
        return replace;
    }


}
