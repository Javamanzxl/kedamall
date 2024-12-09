package com.zxl.gulimall.order.web;

import com.zxl.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author ：zxl
 * @Description: 页面跳转等业务
 * @ClassName: OrderController
 * @date ：2024/12/01 20:47
 */
@Controller
public class WebController {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @ResponseBody
    @GetMapping("/test/createOrder")
    public String createOrderTest(){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn("zxl");
        //给mq发送消息
        String key = "order.create.order";
        rabbitTemplate.convertAndSend("order-event-exchange",key,orderEntity);
        return "ok";
    }
    @GetMapping("/{page}.html")
    public String list(@PathVariable String page){
        return page;
    }
}
