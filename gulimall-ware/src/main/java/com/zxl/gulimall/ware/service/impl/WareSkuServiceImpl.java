package com.zxl.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import com.zxl.common.exception.NoStockException;
import com.zxl.common.to.OrderTo;
import com.zxl.common.to.SkuHasStockTo;
import com.zxl.common.to.SkuInfoTo;
import com.zxl.common.to.mq.StockDetailTo;
import com.zxl.common.to.mq.StockLockedTo;
import com.zxl.common.utils.R;
import com.zxl.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.zxl.gulimall.ware.entity.WareOrderTaskEntity;
import com.zxl.gulimall.ware.feign.OrderFeignService;
import com.zxl.gulimall.ware.feign.ProductFeign;
import com.zxl.gulimall.ware.service.WareOrderTaskDetailService;
import com.zxl.gulimall.ware.service.WareOrderTaskService;
import com.zxl.gulimall.ware.vo.OrderItemVo;
import com.zxl.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.Query;

import com.zxl.gulimall.ware.dao.WareSkuDao;
import com.zxl.gulimall.ware.entity.WareSkuEntity;
import com.zxl.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("wareSkuService")
@Slf4j
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    private WareSkuDao wareSkuDao;
    @Resource
    private ProductFeign productFeign;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private WareOrderTaskService wareOrderTaskService;
    @Resource
    private WareOrderTaskDetailService wareOrderTaskDetailService;
    @Resource
    private OrderFeignService orderFeignService;

    /**
     * 条件分页查询
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<WareSkuEntity> wrapper = new LambdaQueryWrapper<>();
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq(WareSkuEntity::getSkuId, skuId);
        }
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq(WareSkuEntity::getWareId, wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 完成采购添加库存
     *
     * @param wareSkuEntity
     */
    @Transactional
    @Override
    public void addStock(WareSkuEntity wareSkuEntity) {
        //判断如果没有库存记录就是新增操作
        LambdaQueryWrapper<WareSkuEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WareSkuEntity::getSkuId, wareSkuEntity.getSkuId());
        wrapper.eq(WareSkuEntity::getWareId, wareSkuEntity.getWareId());
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(wrapper);
        if (wareSkuEntities == null || wareSkuEntities.isEmpty()) {
            wareSkuEntity.setStockLocked(0);
            //远程查询sku的名字
            try {
                SkuInfoTo skuInfoTo = productFeign.infoBySkuId(wareSkuEntity.getSkuId());
                if (skuInfoTo != null) {
                    wareSkuEntity.setSkuName(skuInfoTo.getSkuName());
                }
            } catch (Exception e) {
                log.error("完成采购添加库存功能出现异常:{}",e.getMessage());
            }
            wareSkuDao.insert(wareSkuEntity);
        } else {
            wareSkuDao.addStock(wareSkuEntity);
        }

    }

    /**
     * 查询sku是否有库存
     *
     * @param skuIds
     * @return
     */
    @Override
    public List<SkuHasStockTo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockTo> tos = skuIds.stream().map(skuId -> {
            SkuHasStockTo skuHasStockTo = new SkuHasStockTo();
            Long stock = wareSkuDao.getSkuStock(skuId);
            skuHasStockTo.setHasStock(stock == null ? false : stock > 0);
            skuHasStockTo.setSkuId(skuId);
            return skuHasStockTo;
        }).toList();
        return tos;
    }

    /**
     * feign远程调用，根据skuId查询是否有库存
     *
     * @param skuId
     * @return
     */
    @Override
    public boolean hasStockBySkuId(Long skuId) {
        WareSkuEntity wareSkuEntity = wareSkuDao.selectOne(new LambdaQueryWrapper<WareSkuEntity>().eq(WareSkuEntity::getSkuId, skuId));
        return wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0;
    }

    /**
     * 锁库存
     *
     * 库存解锁场景：
     *  1）、下单成功，订单过期没有支付被系统自动取消、被用户手动取消。
     *  2）、下订单成功，库存锁定成功，接下来业务失败，导致订单回滚
     *          之前锁定的库存要自动解锁。
     *
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        /**
         * 保存库存工作单
         */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(taskEntity);

        //1.找到每个商品在哪个仓库有库存
        List<OrderItemVo> orderItems = vo.getLocks();
        List<SkuWareHasStock> wares = orderItems.stream().map(item -> {
            SkuWareHasStock w = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            w.setSkuId(skuId);
            //查询这个商品在哪里有库存
            List<Long> wareIds = wareSkuDao.listWareHasStock(skuId);
            w.setWareId(wareIds);
            w.setNum(item.getCount());
            return w;
        }).toList();
        //2.锁定库存
        for (SkuWareHasStock ware : wares) {
            Long skuId = ware.getSkuId();
            List<Long> wareIds = ware.getWareId();
            Integer num = ware.getNum();
            if (wareIds == null || wareIds.isEmpty()) {
                //没有任何仓库有这个商品
                throw new NoStockException(skuId);
            }
            boolean skuStocked = false;
            /**
             * 1.如果每一件商品都锁成功，将当前商品锁定了几件的工作单记录发送给MQ
             * 2.锁定失败，前面保存的工作单信息就回滚了。发送出去的消息，即使要解锁记录，但是去数据库查不到id,所以不用解锁
             *
             */
            for (Long wareId : wareIds) {
                //成功返回1，否则是0
                Long count = wareSkuDao.lockSkuStock(skuId,wareId,num);
                if(count ==1){
                    //锁成功
                    skuStocked = true;
                    //发给MQ消息，锁库存成功
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
                    taskDetailEntity.setSkuId(skuId);
                    taskDetailEntity.setSkuNum(num);
                    taskDetailEntity.setTaskId(taskEntity.getId());
                    taskDetailEntity.setWareId(wareId);
                    taskDetailEntity.setLockStatus(1);
                    wareOrderTaskDetailService.save(taskDetailEntity);
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setTaskId(taskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity,stockDetailTo);
                    //防止回滚以后找不到数据
                    stockLockedTo.setTaskDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange",
                            "stock.locked",stockLockedTo);
                    break;
                }
                //当前仓库锁失败，尝试下一个仓库
            }
            if(!skuStocked){
                //当前商品所有仓库都没有锁住
                throw new NoStockException(skuId);
            }
        }
        //都锁定成功
        return true;
    }

    /**
     * 定时任务，订单关闭后，解锁库存
     * @param order
     */
    @Override
    public void unlockStock(OrderTo order) {
        String orderSn = order.getOrderSn();
        //查一下库存工作单的状态，防止重复解锁
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long taskEntityId = taskEntity.getId();
        //按照工作单id找到没有解锁的库存，进行解锁
        List<WareOrderTaskDetailEntity> taskDetailEntities = wareOrderTaskDetailService
                .list(new LambdaQueryWrapper<WareOrderTaskDetailEntity>()
                .eq(WareOrderTaskDetailEntity::getTaskId, taskEntityId)
                .eq(WareOrderTaskDetailEntity::getLockStatus, 1));
        if(taskDetailEntities!=null && !taskDetailEntities.isEmpty()){
            //解锁库存
            for (WareOrderTaskDetailEntity taskDetailEntity : taskDetailEntities) {
                unlockStock(taskDetailEntity);
            }
        }
    }

    /**
     * 解锁库存方法
     * @param taskDetailEntity
     */
    public void unlockStock(WareOrderTaskDetailEntity taskDetailEntity){
        Long skuId = taskDetailEntity.getSkuId();
        Long wareId = taskDetailEntity.getWareId();
        Integer skuNum = taskDetailEntity.getSkuNum();
        wareSkuDao.releaseLocked(skuId,wareId,skuNum);
        //更新库存工作单状态
        WareOrderTaskDetailEntity taskDetail = new WareOrderTaskDetailEntity();
        taskDetail.setId(taskDetailEntity.getId());
        taskDetail.setLockStatus(2);
        wareOrderTaskDetailService.updateById(taskDetail);
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }
}