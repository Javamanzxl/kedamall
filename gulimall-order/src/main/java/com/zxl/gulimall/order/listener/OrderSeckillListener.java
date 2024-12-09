package com.zxl.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.zxl.common.to.mq.SeckillOrderTo;
import com.zxl.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author ：zxl
 * @Description: 秒杀订单监听器
 * @ClassName: OrderSeckillLisetener
 * @date ：2024/12/09 15:44
 */
@RabbitListener(queues = {"order.seckill.order.queue"})
@Component
@Slf4j
public class OrderSeckillListener {
    @Resource
    private OrderService orderService;
    @RabbitHandler
    public void listener(SeckillOrderTo seckillOrderTo, Message message, Channel channel) throws IOException {
        log.info("收到秒杀订单，正在处理。。。");
        try{
            orderService.createSeckillOrder(seckillOrderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
