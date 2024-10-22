package com.zxl.gulimall.order.dao;

import com.zxl.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:37:39
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
