package com.zxl.gulimall.order.vo;

import com.zxl.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author ：zxl
 * @Description: 订单返回数据
 * @ClassName: SubmitOrderResVo
 * @date ：2024/12/03 10:33
 */
@Data
public class SubmitOrderResVo {
    private OrderEntity orderEntity;
    private Integer code; //错误状态码 0成功
}
