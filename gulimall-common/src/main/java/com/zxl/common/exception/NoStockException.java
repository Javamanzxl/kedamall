package com.zxl.common.exception;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ：zxl
 * @Description: 没有库存异常
 * @ClassName: NoStockException
 * @date ：2024/12/03 19:58
 */
@Getter
@Setter
public class NoStockException extends RuntimeException{

    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品id："+ skuId + "库存不足！");
    }

    public NoStockException(String msg) {
        super(msg);
    }

}
