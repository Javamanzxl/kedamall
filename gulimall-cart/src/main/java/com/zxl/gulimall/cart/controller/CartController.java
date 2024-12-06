package com.zxl.gulimall.cart.controller;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.zxl.gulimall.cart.interceptor.CartInterceptor;
import com.zxl.gulimall.cart.service.CartService;
import com.zxl.gulimall.cart.to.UserInfoTo;
import com.zxl.gulimall.cart.vo.Cart;
import com.zxl.gulimall.cart.vo.CartItem;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: CartController
 * @date ：2024/11/29 13:12
 */
@Controller
public class CartController {
    @Resource
    private CartService cartService;

    /**
     * 远程调用，获取当前用户购物车信息
     * @return
     */
    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItem> getCurrentUserCartItems(){
        return cartService.getCurrentUserCartItems();
    }

    /***
     * 浏览器有一个cookie: user-key:标识用户身份，一个月后过期
     * 如果第一次访问，服务器会给一个user-key
     * 浏览器保存，每次访问该服务器都会带上这个cookie
     *
     * 登陆： session有
     * 没登陆：按照cookie里面的user-key来处理
     * 第一次：如果没有临时用户，创建一个临时用户
     * 购物车页面跳转
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart = cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    /**
     * RedirectAttributes attributes
     *  attributes.addFlashAttribute();将数据放在session中，在页面中取出，但是只能取一次
     *  attributes.addAttribute();将数据放在url后
     * @param skuId
     * @param num
     * @param attributes
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes attributes) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId,num);
        attributes.addAttribute("skuId",skuId);
        return "redirect:http://cart.zxl1027.com/addToCartSuccess.html";
    }

    /**
     * 避免刷新一直提交
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,Model model){
        //重定向到成功页面，再次查询购物车数据即可
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("cartItem",cartItem);
        return "success";
    }

    /**
     * 更新购物车中选中或取消商品
     * @param skuId
     * @param check
     * @return
     */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId")Long skuId,@RequestParam("check") Integer check){
        cartService.checkItem(skuId,check);
        return "redirect:http://cart.zxl1027.com/cart.html";
    }

    /**
     * 修改商品的数量
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId")Long skuId,@RequestParam("num") Integer num){
        cartService.countItem(skuId,num);
        return "redirect:http://cart.zxl1027.com/cart.html";
    }

    /**
     * 删除某个商品
     * @param skuId
     * @return
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId")Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.zxl1027.com/cart.html";
    }
}
