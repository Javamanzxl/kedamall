package com.zxl.gulimall.order;

import com.zxl.gulimall.order.config.AlipayTemplate;
import com.zxl.gulimall.order.entity.OrderEntity;
import com.zxl.gulimall.order.properties.ThreadPoolConfigProperties;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 1.引入RabbitMQ
 *      1).引入amqp场景:RabbitAutoConfiguration就会自动生效
 *      2).给容器中自动配置了 RabbitTemplate,AmqpAdmin,CachingConnectionFactory,RabbitMessagingTemplate
 *      所有属性都是spring.rabbitmq   @ConfigurationProperties(prefix="spring.rabbitmq")
 *      3).给配置文件配置spring.rabbitmq信息
 *      4).@EnableRabbit
 *      5).监听消息:@RabbitListener 必须有@EnableRabbit
 *          @RabbitListener:标在类或者方法上(监听哪些队列)
 *          @RabbitHandler:标在方法上(重载不同的消息类型)
 *
 * @RabbitListener(queues = {"hello-java-queue"})，要声明的方法必须在容器中
 * queues:声明需要监听的队列
 * @param message：原生消息详细信息（头+体）
 * @param orderEntity T<发送消息的类型>spring会自动转换
 * @param channel：是个接口 获取当前传输数据的通道
 * Queue:可以很多人都来监听，只要收到消息，队列删除消息，而且只能有一个收到此消息
 *      场景：
 *            1):订单服务启动多个，同一个消息，只能有一个客户端收到
 *            2):只要一个消息完全处理完，方法运行结束，我们就可以接收到下一个消息
 * @RabbitListener(queues = {"hello-java-queue"})
 * public void receiveMessage(Message message, OrderEntity orderEntity,Channel channel){}
 *
 *2.本地事务失效问题：
 *      同一个对象内事务方法互相调用默认失效，因为绕过了代理对象
 *      事务通过使用代理对象来控制
 *      解决：
 *          使用代理对象来调用事务方法
 *              1）引入aop
 *              2）@EnableAspectJAutoProxy(exposeProxy)
 *              3）本类互调调用对象
 *3.Seata控制分布式事务
 *      1）导入依赖
 *      2）启动seata服务器
 *
 */



@SpringBootApplication
@EnableRabbit
@EnableDiscoveryClient
@EnableRedisHttpSession
@EnableFeignClients
@EnableTransactionManagement
//@EnableAutoDataSourceProxy
@EnableConfigurationProperties(value = {ThreadPoolConfigProperties.class, AlipayTemplate.class})
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
