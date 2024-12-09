package com.zxl.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.zxl.gulimall.order.config.AlipayTemplate;
import com.zxl.gulimall.order.service.OrderService;
import com.zxl.gulimall.order.vo.PayVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author ：zxl
 * @Description: 支付
 * @ClassName: PayWebController
 * @date ：2024/12/06 16:02
 */
@Controller
public class PayWebController {

    @Resource
    private AlipayTemplate alipayTemplate;
    @Resource
    private OrderService orderService;

    /**
     * 1.将支付页让浏览器展示
     * 2.支付成功以后，要跳转到用户的订单列表页
     * @param orderSn
     * @return
     * @throws AlipayApiException
     */

    @ResponseBody
    @GetMapping(value = "/payOrder",produces = "text/html")
    public String payOrder(@RequestParam String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.getOrderPay(orderSn);
        //返回的是一个页面，将此页面直接交给浏览器
        return alipayTemplate.pay(payVo);
    }
}
