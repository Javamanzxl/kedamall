package com.zxl.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.zxl.gulimall.order.entity.OrderEntity;
import com.zxl.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author ：zxl
 * @Description: RM定时关单功能
 * @ClassName: OrderCloseListener
 * @date ：2024/12/06 10:22
 */
@Service
@RabbitListener(queues = "order.release.order.queue")
@Slf4j
public class OrderCloseListener {
    @Resource
    private OrderService orderService;
    @RabbitHandler
    public void orderCloseListener(OrderEntity order, Message message, Channel channel) throws IOException {
        log.info("收到过期的订单信息：准备关闭订单：{}",order.getOrderSn());
        try{
            orderService.orderClose(order);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }
}
