package com.zxl.gulimall.order.web;

import com.zxl.common.exception.ErrorCodeEnum;
import com.zxl.common.exception.NoStockException;
import com.zxl.gulimall.order.service.OrderService;
import com.zxl.gulimall.order.vo.OrderConfirmVo;
import com.zxl.gulimall.order.vo.OrderSubmitVo;
import com.zxl.gulimall.order.vo.SubmitOrderResVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: OderWebController
 * @date ：2024/12/01 21:05
 */
@Controller
@Slf4j
public class OderWebController {
    @Resource
    private OrderService orderService;

    /**
     * 结算页面功能
     *
     * @param model
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", orderConfirmVo);
        return "confirm";
    }

    /**
     * 提交订单功能
     *
     * @param orderSubmitVo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes) {
        try{
            SubmitOrderResVo submitOrderRespVo = orderService.submitOrder(orderSubmitVo);
            if (submitOrderRespVo.getCode() == 0) {
                //下单成功跳到支付选择页
                model.addAttribute("submitOrderResp", submitOrderRespVo);
                return "pay";
            } else {
                //下单失败回到订单确认页
                String msg = "msg";
                switch (submitOrderRespVo.getCode()) {
                    case 1:
                        msg += "令牌校验失败";
                        break;
                    case 2:
                        msg += "锁失败";
                        break;
                    case 3:
                        msg += "金额对比失败";
                        break;
                    case 4:
                        msg += "验证令牌为空";
                        break;
                }
                redirectAttributes.addFlashAttribute("msg", msg);
                return "redirect:http://order.zxl1027.com/toTrade";
            }
        }catch (Exception e){
            if(e instanceof NoStockException){
                String message = e.getMessage();
                redirectAttributes.addFlashAttribute("msg",message);
            }else{
                log.error(e.getMessage());
                redirectAttributes.addFlashAttribute("msg", ErrorCodeEnum.UNKNOW_EXCEPTION.getMessage());
            }
            return "redirect:http://order.zxl1027.com/toTrade";
        }

    }

}
