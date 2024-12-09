package com.zxl.gulimall.order.config;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.zxl.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ：zxl
 * @Description: rabbitmq创建配置,容器中的Binding,Queue,Exchange会自动创建
 * RabbitMQ 只要有。@Bean声明属性发生变化也不会覆盖
 * @ClassName: RabbitMQCreateConfig
 * @date ：2024/12/05 18:23
 */
@Configuration
public class RabbitMQCreateConfig {
    //@Bean Binding,Queue,Exchange

    @Bean
    public Queue orderDelayQueue(){
        /**
         * 设置
         * x-dead-letter-exchange
         * x-dead-letter-routing-key
         * x-message-ttl
         */
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",60000);
        return new Queue("order.delay.queue", true, false, false, arguments);
    }
    @Bean
    public Queue orderReleaseOrderQueue(){
        return new Queue("order.release.order.queue",true,false,false,null);
    }
    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange("order-event-exchange",true,false,null);
    }
    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE
                ,"order-event-exchange","order.create.order",null);
    }
    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE
                ,"order-event-exchange","order.release.order",null);
    }

    /**
     * 订单关闭直接和库存释放进行绑定
     * @return
     */
    @Bean
    public Binding OrderReleaseOtherBinding(){
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE
                ,"order-event-exchange","order.release.other.#",null);
    }

    /**
     * 秒杀消息队列
     * @return
     */
    @Bean
    public Queue orderSeckillOrderQueue(){
        return new Queue("order.seckill.order.queue",true,false,false,null);
    }

    @Bean
    public Binding orderSeckillOrderBinding(){
        return new Binding("order.seckill.order.queue", Binding.DestinationType.QUEUE
                ,"order-event-exchange","order.seckill.order",null);
    }
}
