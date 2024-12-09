package com.zxl.gulimall.order.config;

import com.rabbitmq.client.ReturnCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
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
    /**
     * 定制化RabbitTemplate
     * 1.服务端(Broker)收到消息就回调
     *      1）publisher-confirm-type: correlated
     *      2) rabbitTemplate.setConfirmCallback
     * 2.消息正确抵达队列进行回调
     *      1）    publisher-returns: true
     *             template:
     *                mandatory: true
     *      2）rabbitTemplate.setReturnsCallback
     * 3.消费端确认，保证每个消息被正确消费，此时才可以删除这个消息
     *      1）默认是自动确认的，只要消息接到，客户端会自动确认，服务端就会移除这个消息
     *          问题：收到很多消息，自动回复给服务器ack,但是只有一个消息处理成功，其他消息则会丢失
     *          解决：手动确认，只要我们没有明确告诉mq，消息被签收。即ack,消息则一直就是unacked状态，即使服务器宕机，消息不会丢失，
     *          消息会变为ready状态
     *       2）如何签收
     *       //deliveryTag：channel内自增
     *       long deliveryTag = message.getMessageProperties().getDeliveryTag();
     *       //手动ack,false表示非批量模式，true表示批量模式
     *       channel.basicAck(deliveryTag,false);
     *       //手动拒绝
     *       channel.basicNack(deliveryTag,multiple,requeue);或channel.basicReject(deliveryTag,requeue);
     *       requeue:false拒绝不入队，true拒绝重新入队
     *       区别:前面可以选择批量拒绝
     *
     */
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
                    //出错了，修改数据库当前消息的状态
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
                //出错了，修改数据库当前消息的状态
                log.error("消息从exchange发送到queue失败，失败信息:{}",returnedMessage);
            }
        });
    }
}
