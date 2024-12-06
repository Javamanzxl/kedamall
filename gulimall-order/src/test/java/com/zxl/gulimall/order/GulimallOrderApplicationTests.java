package com.zxl.gulimall.order;

import com.zxl.gulimall.order.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@SpringBootTest
@Slf4j
class GulimallOrderApplicationTests {
    @Resource
    private AmqpAdmin amqpAdmin;
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 1.如何创建Exchange、Queue、Binding
     *      1）、使用AmqpAdmin进行创建
     * 2.如何收发消息
     */

    /**
     * 创建交换机
     */
    @Test
    void createExchange() {
        DirectExchange directExchange = new DirectExchange("hello.java.exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("exchange{}创建成功","hello.java.exchange");
    }

    /**
     * 创建队列
     */
    @Test
    void createQueue(){
        Queue queue = new Queue("hello.java.queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue:{}创建成功","hello.java.queue");
    }

    /**
     * 创建关系
     */
    @Test
    void createBinding(){

        String routeKey = "hello.java.queue";
        /**
         * String destination, 目的地,即交换机或者队列的名字
         * DestinationType destinationType, 目的地类型(交换机或队列)
         * String exchange,交换机
         * String routingKey,路由key
         * @Nullable Map<String, Object> arguments 自定义参数
         * 将exchange指定的交换机和destination目的地进行绑定，使用routingKey作为路由键
         *
         */
        Binding binding = new Binding("hello.java.queue",
                        Binding.DestinationType.QUEUE,
                        "hello.java.exchange",
                        routeKey,null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding:{}创建成功","hello.java.queue");
    }

    /**
     * 测试发消息
     */
    @Test
    void sendMessage() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setNote("你好");
        orderEntity.setBillContent("你好");
        String message = "hello,world";
        Message message1 = new Message(message.getBytes(StandardCharsets.UTF_8));
//        rabbitTemplate.send("hello.java.exchange",
//                "hello.java.queue",message1);
//        rabbitTemplate.convertAndSend("hello.java.exchange",
//                "hello.java.queue",message);
        //如果发送的消息是个对象，我们必须使用序列化机制，将对象写出去，对象必须实现Serializable
        rabbitTemplate.convertAndSend("hello.java.exchange",
                "hello.java.queue",orderEntity);
        log.info("消息发送完成:{}",orderEntity.toString());
    }

    /**
     * 测试接收消息
     */
    @Test
    void receiveMessage() {

    }


}
