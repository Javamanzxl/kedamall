package com.zxl.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zxl.common.constant.OrderConstant;
import com.zxl.common.exception.NoStockException;
import com.zxl.common.to.MemberTo;
import com.zxl.common.to.OrderTo;
import com.zxl.common.to.mq.SeckillOrderTo;
import com.zxl.common.utils.R;
import com.zxl.gulimall.order.dao.PaymentInfoDao;
import com.zxl.gulimall.order.entity.OrderItemEntity;
import com.zxl.gulimall.order.entity.PaymentInfoEntity;
import com.zxl.gulimall.order.enume.OrderStatusEnum;
import com.zxl.gulimall.order.feign.CartFeignService;
import com.zxl.gulimall.order.feign.MemberFeignService;
import com.zxl.gulimall.order.feign.ProductFeignService;
import com.zxl.gulimall.order.feign.WareFeignService;
import com.zxl.gulimall.order.interceptor.LoginUserInterceptor;
import com.zxl.gulimall.order.service.OrderItemService;
import com.zxl.gulimall.order.to.OrderCreateTo;
import com.zxl.gulimall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.Query;

import com.zxl.gulimall.order.dao.OrderDao;
import com.zxl.gulimall.order.entity.OrderEntity;
import com.zxl.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> submitThreadLocal = new ThreadLocal<>();
    @Resource
    private MemberFeignService memberFeignService;
    @Resource
    private CartFeignService cartFeignService;
    @Resource
    private WareFeignService wareFeignService;
    @Resource
    private ThreadPoolExecutor executor;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private OrderDao orderDao;
    @Resource
    private OrderItemService orderItemService;
    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private PaymentInfoDao paymentInfoDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 封装订单确认页需要的数据
     *
     * @return
     */
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        MemberTo member = LoginUserInterceptor.loginUser.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        OrderConfirmVo orderConfirm = new OrderConfirmVo();
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            //1.远程查询当前会员的地址
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(member.getId());
            orderConfirm.setAddress(address);
        }, executor);
        CompletableFuture<Void> orderItemFuture = CompletableFuture.runAsync(() -> {
            //2.远程查询当前会员的购物车信息
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> orderItems = cartFeignService.getCurrentUserCartItems();
            orderConfirm.setItems(orderItems);
            //feign在远程调用之前要构造请求，会调用很多拦截器
            //RequestInterceptor interceptor : requestInterceptors
        }, executor).thenRunAsync(() -> {
            //查询库存信息
            List<OrderItemVo> items = orderConfirm.getItems();
            List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).toList();
            List<SkuHasStockVo> skusHasStock = wareFeignService.getSkusHasStock(skuIds);
            if (skusHasStock != null) {
                Map<Long, Boolean> stocks = skusHasStock.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
                orderConfirm.setStocks(stocks);
            }
        }, executor);
        //3.查询用户积分
        Integer integration = member.getIntegration();
        orderConfirm.setIntegration(integration);
        //4.价格等信息在实体类中自动计算
        //TODO：5.防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        orderConfirm.setOrderToken(token);
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + member.getId(), token, 30, TimeUnit.MINUTES);
        CompletableFuture.allOf(addressFuture, orderItemFuture).get();
        return orderConfirm;
    }

    /**
     * 下单
     *
     * @param orderSubmitVo
     * @return
     */
    @Transactional
    @Override
    public SubmitOrderResVo submitOrder(OrderSubmitVo orderSubmitVo) {
        MemberTo member = LoginUserInterceptor.loginUser.get();
        submitThreadLocal.set(orderSubmitVo);
        Long userId = member.getId();
        SubmitOrderResVo res = new SubmitOrderResVo();
        res.setCode(0);
        //1.验证令牌(令牌的对比和删除保证原子性)
        //这个脚本失败返回0，成功1
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = orderSubmitVo.getOrderToken();
        //原子验证令牌和删除令牌
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), List.of(OrderConstant.USER_ORDER_TOKEN_PREFIX + userId), orderToken);
        if (result != null) {
            if (result == 0) {
                //验证失败
                res.setCode(1);
                return res;
            } else {
                //验证成功
                //下单，创订单，验价格，锁库存。
                //1.创建订单
                OrderCreateTo order = createOrder();
                //2.验价
                BigDecimal payAmount = order.getPayPrice();
                BigDecimal price = orderSubmitVo.getPayPrice();
                if (Math.abs(
                        payAmount.subtract(price).doubleValue()) < 0.01) {
                    //金额对比成功
                    //3.保存订单
                    saveOrder(order);
                    //4.锁库存,只要有异常回滚数据
                    //订单号，订单项(skuId,skuName,num)
                    WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                    List<OrderItemEntity> orderItems = order.getOrderItem();
                    List<OrderItemVo> orderItemVos = orderItems.stream().map(item -> {
                        OrderItemVo orderItemVo = new OrderItemVo();
                        orderItemVo.setSkuId(item.getSkuId());
                        orderItemVo.setCount(item.getSkuQuantity());
                        orderItemVo.setTitle(item.getSkuName());
                        return orderItemVo;
                    }).toList();
                    wareSkuLockVo.setLocks(orderItemVos);
                    wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                    //为了保证高并发，库存服务自己回滚。如果失败了可以发消息给库存服务
                    //库存服务本身也可以使用自动解锁模式，消息队列
                    R r = wareFeignService.orderLockStock(wareSkuLockVo);
                    if (r.getCode() == 0) {
                        //锁成功
                        res.setOrderEntity(order.getOrder());
                        //订单创建成功，给MQ发消息
                        rabbitTemplate.convertAndSend("order-event-exchange",
                                "order.create.order", order.getOrder());
                        return res;
                    } else {
                        //锁失败
                        String msg = (String) r.get("msg");
                        throw new NoStockException(msg);
                    }
                } else {
                    //金额对比失败
                    res.setCode(3);
                    return res;
                }

            }
        }
//        String token = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + userId);
//        if(orderToken!=null && orderToken.equals(token)){
//            //令牌验证通过
//            redisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + userId);
//            res.setCode(1);
//        }else{
//            //不通过
//            res.setCode(0);
//        }
        res.setCode(4);
        return res;
    }

    /**
     * 保存订单数据
     *
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        List<OrderItemEntity> orderItems = order.getOrderItem();
        orderEntity.setModifyTime(new Date());
        orderDao.insert(orderEntity);
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 创建订单方法
     *
     * @return
     */

    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();

        //1.构建订单
        //生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity order = buildOrder(orderSn);
        orderCreateTo.setOrder(order);
        //2.获取到所有的订单项目
        List<OrderItemEntity> orderItems = buildOrderItems(orderSn);
        orderCreateTo.setOrderItem(orderItems);
        //3.计算价格积分等相关信息
        computePrice(order, orderItems);
        orderCreateTo.setPayPrice(order.getPayAmount());
        orderCreateTo.setFare(order.getFreightAmount());
        return orderCreateTo;
    }

    /**
     * 计算价格积分等相关信息
     *
     * @param order
     * @param orderItems
     */
    private void computePrice(OrderEntity order, List<OrderItemEntity> orderItems) {
        //1.订单价格相关计算
        //订单总额
        BigDecimal totalAmount = new BigDecimal("0.0");
        BigDecimal couponAmount = new BigDecimal("0.0");
        BigDecimal integrationAmount = new BigDecimal("0.0");
        BigDecimal promotionAmount = new BigDecimal("0.0");
        BigDecimal giftIntegration = new BigDecimal("0.0");
        BigDecimal giftGrowth = new BigDecimal("0.0");
        for (OrderItemEntity orderItem : orderItems) {
            BigDecimal realAmount = orderItem.getRealAmount();
            totalAmount = totalAmount.add(realAmount);
            couponAmount = couponAmount.add(orderItem.getCouponAmount());
            integrationAmount = integrationAmount.add(orderItem.getIntegrationAmount());
            promotionAmount = promotionAmount.add(orderItem.getPromotionAmount());
            giftIntegration = giftIntegration.add(new BigDecimal(orderItem.getGiftIntegration().toString()));
            giftGrowth = giftGrowth.add(new BigDecimal(orderItem.getGiftGrowth().toString()));
        }
        order.setTotalAmount(totalAmount);
        order.setPromotionAmount(promotionAmount);
        order.setCouponAmount(couponAmount);
        order.setIntegrationAmount(integrationAmount);
        //应付总额
        order.setPayAmount(totalAmount.add(order.getFreightAmount()));
        //2.设置积分等信息
        order.setIntegration(giftIntegration.intValue());
        order.setGrowth(giftGrowth.intValue());
        order.setDeleteStatus(0);//未删除
    }

    /**
     * 构建订单
     *
     * @param orderSn
     * @return
     */

    private OrderEntity buildOrder(String orderSn) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        //1.设置会员信息
        MemberTo member = LoginUserInterceptor.loginUser.get();
        orderEntity.setMemberId(member.getId());
        orderEntity.setMemberUsername(member.getUsername());
        //2.获取收货地址信息
        OrderSubmitVo orderSubmitVo = submitThreadLocal.get();
        R r = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareVo = r.getData(new TypeReference<FareVo>() {
        });
        //设置运费信息
        orderEntity.setFreightAmount(fareVo.getFare());
        //设置收货人信息
        orderEntity.setReceiverProvince(fareVo.getAddress().getProvince());
        orderEntity.setReceiverCity(fareVo.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareVo.getAddress().getName());
        orderEntity.setReceiverPhone(fareVo.getAddress().getPhone());
        orderEntity.setReceiverPostCode(fareVo.getAddress().getPostCode());
        orderEntity.setReceiverRegion(fareVo.getAddress().getRegion());
        //设置订单状态
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);
        return orderEntity;
    }

    /**
     * 构建所有订单项
     *
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && !currentUserCartItems.isEmpty()) {
            //构建所有订单项
            return currentUserCartItems.stream().map(item -> {
                //构建一个订单项
                OrderItemEntity orderItemEntity = buildOrderItem(item);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).toList();
        }
        return null;
    }

    /**
     * 构建一个订单项目
     *
     * @param orderItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo orderItem) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //spu
        Long skuId = orderItem.getSkuId();
        SpuInfoVo spuInfo = productFeignService.getSpuInfoBySkuId(skuId);
        orderItemEntity.setSpuId(spuInfo.getId());
        orderItemEntity.setSpuName(spuInfo.getSpuName());
        orderItemEntity.setSpuPic(spuInfo.getSpuDescription());
        String brandName = productFeignService.getBrandNameById(spuInfo.getBrandId());
        orderItemEntity.setSpuBrand(brandName);
        orderItemEntity.setCategoryId(spuInfo.getCatalogId());
        //sku
        orderItemEntity.setSkuId(orderItem.getSkuId());
        orderItemEntity.setSkuName(orderItem.getTitle());
        orderItemEntity.setSkuPic(orderItem.getImage());
        orderItemEntity.setSkuPrice(orderItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(orderItem.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttr);
        orderItemEntity.setSkuQuantity(orderItem.getCount());
        //优惠信息[不做]
        //积分信息
        orderItemEntity.setGiftGrowth(orderItem.getPrice().multiply(new BigDecimal(orderItem.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(orderItem.getPrice().multiply(new BigDecimal(orderItem.getCount().toString())).intValue());
        //订单项目价格信息
        orderItemEntity.setPromotionAmount(new BigDecimal("0.0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0.0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0.0"));
        BigDecimal orignPrice = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        BigDecimal realAmount = orignPrice.subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getIntegrationAmount()).subtract(orderItemEntity.getPromotionAmount());
        orderItemEntity.setRealAmount(realAmount);
        return orderItemEntity;
    }

    /**
     * 远程调用，根据orderSn查询订单信息
     *
     * @param orderSn
     * @return
     */
    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return orderDao.selectOne(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderSn, orderSn));
    }

    /**
     * 关闭订单功能
     *
     * @param order
     */
    @Override
    public void orderClose(OrderEntity order) {
        //先查询当前订单的最新状态
        String orderSn = order.getOrderSn();
        OrderEntity dbOrder = orderDao.selectOne(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderSn, orderSn));
        //关单
        if (dbOrder != null && dbOrder.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            OrderEntity updateOrder = new OrderEntity();
            updateOrder.setStatus(OrderStatusEnum.CANCLED.getCode());
            updateOrder.setId(dbOrder.getId());
            orderDao.updateById(updateOrder);
            //关闭订单后再给解锁库存发个消息
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(order, orderTo);
            try {
                //TODO: 保证消息一定会发送出去，每一个消息做好日志记录(给数据库保存每一个消息的详细信息)
                //TODO: 定期扫描数据库将失败的消息重新发送
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (Exception e) {
                //TODO: 将没发送成功的消息进行重试发送
            }
        }
    }

    /**
     * 获取当前订单的支付信息
     *
     * @param orderSn
     * @return
     */
    @Override
    public PayVo getOrderPay(String orderSn) {
        OrderEntity orderEntity = orderDao.selectOne(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderSn, orderSn));
        PayVo payVo = new PayVo();
        BigDecimal payAmount = orderEntity.getPayAmount().setScale(2, RoundingMode.UP);
        payVo.setTotal_amount(payAmount.toString());
        payVo.setOut_trade_no(orderEntity.getOrderSn());
        List<OrderItemEntity> orderItems = orderItemService.list(new LambdaQueryWrapper<OrderItemEntity>()
                .eq(OrderItemEntity::getOrderSn, orderSn));
        OrderItemEntity orderItem = orderItems.get(0);
        payVo.setSubject(orderItem.getSkuName());
        payVo.setBody(orderItem.getSkuAttrsVals());
        return payVo;
    }

    /**
     * 查询登陆用户的订单信息
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberTo member = LoginUserInterceptor.loginUser.get();
        Long memberId = member.getId();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getMemberId, memberId).orderByDesc(OrderEntity::getId));
        if (page != null) {
            List<OrderEntity> orderList = page.getRecords().stream().map(order -> {
                List<OrderItemEntity> orderItems = orderItemService.list(new LambdaQueryWrapper<OrderItemEntity>()
                        .eq(OrderItemEntity::getOrderSn, order.getOrderSn()));
                order.setOrderItems(orderItems);
                return order;
            }).toList();
            page.setRecords(orderList);
            return new PageUtils(page);
        }
        return null;
    }

    /**
     * 处理支付宝支付结果
     *
     * @param vo
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        //1.保存交易流水
        PaymentInfoEntity paymentInfo = new PaymentInfoEntity();
        paymentInfo.setAlipayTradeNo(vo.getTrade_no());
        paymentInfo.setOrderSn(vo.getOut_trade_no());
        paymentInfo.setPaymentStatus(vo.getTrade_status());
        paymentInfo.setCallbackTime(vo.getNotify_time());
        paymentInfoDao.insert(paymentInfo);
        //2.修改订单状态信息
        String tradeStatus = vo.getTrade_status();
        if (tradeStatus.equals("TRADE_SUCCESS") || tradeStatus.equals("TRADE_FINISHED")) {
            String orderSn = vo.getOut_trade_no();
            orderDao.updateOrderStatus(orderSn, OrderStatusEnum.PAYED.getCode());
            return "success";
        }
        return "false";
    }

    /**
     * 创建秒杀订单
     *
     * @param seckillOrderTo
     */
    @Override
    public void createSeckillOrder(SeckillOrderTo seckillOrderTo) {
        //保存订单信息
        OrderEntity order = new OrderEntity();
        order.setOrderSn(seckillOrderTo.getOrderSn());
        order.setMemberId(seckillOrderTo.getMemberId());
        order.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = seckillOrderTo.getSeckillPrice().multiply(new BigDecimal(seckillOrderTo.getNum().toString()));
        order.setPayAmount(multiply);
        orderDao.insert(order);
        //保存订单项目信息
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrderSn(seckillOrderTo.getOrderSn());
        orderItem.setRealAmount(multiply);
        orderItem.setSkuQuantity(seckillOrderTo.getNum());
        SpuInfoVo spuInfo = productFeignService.getSpuInfoBySkuId(seckillOrderTo.getSkuId());
        orderItem.setSpuPic(spuInfo.getSpuDescription());
        orderItem.setSpuName(spuInfo.getSpuName());
        orderItem.setSpuId(spuInfo.getId());
        orderItem.setCategoryId(spuInfo.getCatalogId());
        orderItemService.save(orderItem);
    }
}