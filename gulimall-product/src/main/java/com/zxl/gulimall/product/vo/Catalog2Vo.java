package com.zxl.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: Catelog2Vo
 * @date ：2024/11/15 10:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catalog2Vo {
    //一级父分类id
    private String catalog1Id;
    //三级子分类
    private List<Catalog3Vo> catalog3List;
    private String id;
    private String name;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Catalog3Vo{
        //父分类，2级分类id
        private String catalog2Id;
        private String id;
        private String name;
    }
}
