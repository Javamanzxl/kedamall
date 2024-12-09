package com.zxl.gulimall.seckill.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ：zxl
 * @Description: rabbit的消息转换器
 * @ClassName: RabbitMessageConverter
 * @date ：2024/11/30 17:03
 */
@Configuration
public class RabbitMessageConverter {
    /**
     * 配置消息序列化转化器
     *
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
