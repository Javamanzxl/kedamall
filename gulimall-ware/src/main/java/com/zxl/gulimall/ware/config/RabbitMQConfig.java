package com.zxl.gulimall.ware.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author ：zxl
 * @Description: RabbitMQ配置类，配置消息确认机制
 * @ClassName: RabbitMQConfig
 * @date ：2024/11/30 15:47
 */
@Configuration
@Slf4j
public class RabbitMQConfig {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @PostConstruct //RabbitMQConfig对象创建完成以后，执行这个方法
    public void initRabbitTemplate(){
        //设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *
             * @param correlationData 当前消息的唯一关联数据(这个是消息的唯一id),需要在发送消息时设置
             * @param b 即ack,表示消息是否成功收到，只要消息抵达Broker就ack=true
             * @param s 即cause,表示失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                if(!b){
                    log.error("消息抵达服务器(Broker)失败，失败原因:{}",s);
                }
            }
        });
        //设置消息抵达队列的确认回调
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            /**
             * 只要消息没有投递给指定的队列，就触发失败回调
             * @param returnedMessage 投递失败的消息
             */
            @Override
            public void returnedMessage(ReturnedMessage returnedMessage) {
                log.error("消息从exchange发送到queue失败，失败信息:{}",returnedMessage);
            }
        });
    }
}
