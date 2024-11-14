package com.zxl.gulimall.search.service.serviceImpl;

import com.alibaba.fastjson.JSON;
import com.zxl.common.to.es.SkuEsModel;
import com.zxl.gulimall.search.config.GulimallElasticSearchConfig;
import com.zxl.gulimall.search.constant.EsConstant;
import com.zxl.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author ：zxl
 * @Description: 商品上架服务
 * @ClassName: ProductSaveServiceImpl
 * @date ：2024/11/14 18:18
 */
@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {
    @Resource
    private RestHighLevelClient client;
    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        //数据保存到es中
        //1.给es中建立索引product,建立好映射关系
        //2.给es中保存数据
        //BulkRequest bulkRequest, RequestOptions options

        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            //构造保存请求
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            String jsonString = JSON.toJSONString(skuEsModel);
            indexRequest.source(jsonString, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = client.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        //TODO:如果错误处理
        boolean b = bulk.hasFailures();
        List<String> errorIds = Arrays.stream(bulk.getItems()).map(BulkItemResponse::getId).toList();
        log.info("商品上架完成:{},返回数据:{}",errorIds,bulk.toString());
        return b;
    }
}
