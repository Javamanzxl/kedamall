package com.zxl.gulimall.ware.config;

import com.rabbitmq.client.Channel;
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
    @Bean
    public Exchange stockEventExchange(){
        return new TopicExchange("stock-event-exchange"
                ,true
                ,false);
    }
    @Bean
    public Queue stockReleaseStockQueue(){
        return new Queue("stock.release.stock.queue"
        ,true,false,false);
    }
    @Bean
    public Queue stockDelayQueue(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","stock-event-exchange");
        arguments.put("x-dead-letter-routing-key","stock.release");
        arguments.put("x-message-ttl",120000);
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }
    @Bean
    public Binding stockLockedBinding(){
        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE
        ,"stock-event-exchange","stock.locked",null);
    }
    @Bean
    public Binding stockReleaseBinding(){
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE
                ,"stock-event-exchange","stock.release.#",null);
    }
}
