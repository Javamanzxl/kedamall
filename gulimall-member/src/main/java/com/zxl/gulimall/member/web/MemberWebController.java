package com.zxl.gulimall.member.web;

import com.alibaba.fastjson.TypeReference;
import com.zxl.common.to.MemberTo;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.R;
import com.zxl.gulimall.member.feign.OrderFeignService;
import com.zxl.gulimall.member.interceptor.LoginUserInterceptor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.params.Params;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ：zxl
 * @Description: 订单列表页
 * @ClassName: MemberWebController
 * @date ：2024/12/06 16:32
 */
@Controller
public class MemberWebController {

    @Resource
    private OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, Model model, HttpServletRequest request) {
        //获取到支付宝给我们传过来的所有请求数据
        //验证签名，如果正确去修改

        //查出当前登陆的用户的所用订单列表数据
        MemberTo member = LoginUserInterceptor.loginUser.get();
        Long id = member.getId();
        Map<String, Object> params = new HashMap<>();
        params.put("page", pageNum.toString());
        R r = orderFeignService.listWithItem(params);
        model.addAttribute("orders", r);
        return "orderList";
    }
}
