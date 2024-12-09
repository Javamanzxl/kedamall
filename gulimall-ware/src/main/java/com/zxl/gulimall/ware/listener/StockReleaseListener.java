package com.zxl.gulimall.ware.listener;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.zxl.common.to.OrderTo;
import com.zxl.common.to.mq.StockDetailTo;
import com.zxl.common.to.mq.StockLockedTo;
import com.zxl.common.utils.R;
import com.zxl.gulimall.ware.dao.WareSkuDao;
import com.zxl.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.zxl.gulimall.ware.entity.WareOrderTaskEntity;
import com.zxl.gulimall.ware.feign.OrderFeignService;
import com.zxl.gulimall.ware.service.WareOrderTaskDetailService;
import com.zxl.gulimall.ware.service.WareOrderTaskService;
import com.zxl.gulimall.ware.service.WareSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author ：zxl
 * @Description: 库存解锁MQ监听器
 * @ClassName: StockReleaseListener
 * @date ：2024/12/05 21:19
 */
@Service
@RabbitListener(queues = "stock.release.stock.queue")
@Slf4j
public class StockReleaseListener {
    @Resource
    private WareOrderTaskService wareOrderTaskService;
    @Resource
    private WareOrderTaskDetailService wareOrderTaskDetailService;
    @Resource
    private OrderFeignService orderFeignService;
    @Resource
    private WareSkuService wareSkuService;
    @Resource
    private WareSkuDao wareSkuDao;
    /**
     * 监听RM的队列，自动解锁库存，解锁失败，要告诉服务器解锁失败
     * @param to
     * @param message
     * @param channel
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        log.info("收到解锁库存的消息");
        Long taskId = to.getTaskId();
        StockDetailTo taskDetail = to.getTaskDetail();
        Long taskDetailId = taskDetail.getId();
        //1.查询数据库关于这个订单的锁定库存信息
        WareOrderTaskDetailEntity taskDetailEntity = wareOrderTaskDetailService.getById(taskDetailId);
        if(taskDetailEntity!=null && taskDetailEntity.getLockStatus()==1){
            /**
             * 有信息,证明库存锁定成功，但是要不要解锁，还要看订单情况
             *     1)没有这个订单，必须解锁
             *     2)如果有这个订单。不是解锁库存，要看订单状态
             *            2.1）状态已取消，解锁库存
             *            2.2) 状态没取消，不能解锁库存
             */
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(taskId);
            String orderSn = taskEntity.getOrderSn();
            //远程查询：根据orderSn查询订单状态
            R r = orderFeignService.getOrderByOrderSn(orderSn);
            if(r.getCode()==0){
                OrderTo order = r.getData(new TypeReference<OrderTo>() {});
                if(order==null || order.getStatus() == 4){
                    //解锁库存
                    wareSkuService.unlockStock(taskDetailEntity);
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
                }
            }else{
                //因为自己调远程方法失败，把消息放回队列
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
                throw new RuntimeException("远程服务失败");
            }

        }else{
            //无需解锁
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
    }


    /**
     * 订单关闭后，发消息给mq,此处接收消息，处理业务
     * 防止订单服务卡顿，库存消息优先到期。查订单状态新建状态，什么都不做
     * 导致订单卡顿，永远不能解锁库存
     * @param order
     * @param message
     * @param channel
     */
    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo order,Message message,Channel channel) throws IOException {
        log.info("订单关闭，准备解锁库存");
        try{
            wareSkuService.unlockStock(order);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

}
