package com.zxl.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.zxl.common.constant.CartConstant;
import com.zxl.common.to.SkuInfoTo;
import com.zxl.gulimall.cart.feign.ProductFeignService;
import com.zxl.gulimall.cart.interceptor.CartInterceptor;
import com.zxl.gulimall.cart.service.CartService;
import com.zxl.gulimall.cart.to.UserInfoTo;
import com.zxl.gulimall.cart.vo.Cart;
import com.zxl.gulimall.cart.vo.CartItem;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: CartServiceImpl
 * @date ：2024/11/29 13:07
 */
@Service
public class CartServiceImpl implements CartService {
    @Resource
    private RedisTemplate<String,String> redisTemplate;
    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private ThreadPoolExecutor executor;

    /**
     * 添加购物车
     *
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());
        if (!StringUtils.isEmpty(res)) {
            //购物车中有商品，修改数量
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
        CartItem cartItem = new CartItem();
        //1.远程查询要添加新商品的信息
        CompletableFuture<Void> getSkuInfoFuture = CompletableFuture.runAsync(() -> {
            SkuInfoTo skuInfo = productFeignService.infoBySkuId(skuId);
            cartItem.setSkuId(skuId);
            cartItem.setImage(skuInfo.getSkuDefaultImg());
            cartItem.setCount(num);
            cartItem.setCheck(true);
            cartItem.setTitle(skuInfo.getSkuTitle());
            cartItem.setPrice(skuInfo.getPrice());
        }, executor);
        //2.远程查询sku的组合信息
        CompletableFuture<Void> getSkuAttrFuture = CompletableFuture.runAsync(() -> {
            List<String> attrValues = productFeignService.getSkuSaleAttrValues(skuId);
            cartItem.setSkuAttr(attrValues);
        }, executor);
        //等待所有任务都完成
        CompletableFuture.allOf(getSkuInfoFuture, getSkuAttrFuture).get();
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
        return cartItem;
    }

    /**
     * 获取到要操作的购物车
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfo = CartInterceptor.threadLocal.get();
        //1.判断用临时购物车还是用户购物车
        String cartKey = "";
        if (userInfo.getUserId() != null) {
            cartKey = CartConstant.CART_PREFIX + userInfo.getUserId();
        } else {
            cartKey = CartConstant.CART_PREFIX + userInfo.getUserKey();
        }
        return redisTemplate.boundHashOps(cartKey);
    }

    /**
     * 获取购物车中的购物项
     *
     * @param skuId
     * @return
     */
    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());
        return JSON.parseObject(res, CartItem.class);
    }

    /**
     * 获取购物车
     *
     * @return
     */
    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        List<CartItem> cartItems;
        //1.区分登录和不登录状态
        UserInfoTo userInfo = CartInterceptor.threadLocal.get();
        if (userInfo.getUserId() != null) {
            //登录状态
            if (userInfo.getUserKey() != null) {
                String temp = CartConstant.CART_PREFIX + userInfo.getUserKey();
                List<CartItem> tempCartItems = getCartItems(temp);
                if (tempCartItems != null && !tempCartItems.isEmpty()) {
                    for (CartItem item : tempCartItems) {
                        addToCart(item.getSkuId(), item.getCount());
                    }
                }
                clearCart(temp);
            }
            String cartKey = CartConstant.CART_PREFIX + userInfo.getUserId();
            cartItems = getCartItems(cartKey);
        } else {
            //游客状态
            String cartKey = CartConstant.CART_PREFIX + userInfo.getUserKey();
            cartItems = getCartItems(cartKey);
        }
        cart.setItems(cartItems);
        //TODO:先封装0，还没做到这个功能
        cart.setReduce(new BigDecimal("0"));
        return cart;
    }

    /**
     * 查询所有cartItem购物项
     *
     * @param cartKey
     * @return
     */
    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> cartOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = cartOps.values();
        if (values != null && !values.isEmpty()) {
            return values.stream().map(value -> JSON.parseObject(value.toString(), CartItem.class)).toList();
        }
        return null;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    /**
     * 更新购物车选择或取消的商品
     * @param skuId
     * @param check
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
    }

    /**
     * 修改商品的数量
     * @param skuId
     * @param num
     */
    @Override
    public void countItem(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
    }

    /**
     * 删除某个商品
     * @param skuId
     */
    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }
}
