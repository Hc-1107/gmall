package com.dcs.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dcs.gmall.annotation.LoginRequire;
import com.dcs.gmall.bean.*;
import com.dcs.gmall.bean.enums.OrderStatus;
import com.dcs.gmall.bean.enums.ProcessStatus;
import com.dcs.gmall.service.BaseService;
import com.dcs.gmall.service.CartService;
import com.dcs.gmall.service.OrderService;
import com.dcs.gmall.service.UserService;
import com.dcs.gmall.util.HttpClientUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@CrossOrigin
public class OrderController {

    @Reference
    private UserService userService;

    @Reference
    private CartService cartService;

    @Reference
    private OrderService orderService;

    @Reference
    private BaseService baseService;

    @RequestMapping("trade")
    @LoginRequire
    public String listUserAddresses(HttpServletRequest request){

        String userId = (String) request.getAttribute("userId");

        List<UserAddress> userAddressList = userService.listUserAddressesByUserId(userId);

        request.setAttribute("userAddressList", userAddressList);

        List<CartInfo> cartCheckedList = cartService.getCartCheckedList(userId);

        List<OrderDetail> orderDetailList=new ArrayList<>(cartCheckedList.size());
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetailList.add(orderDetail);
        }
        request.setAttribute("orderDetailList",orderDetailList);
        OrderInfo orderInfo=new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);


        return "trade";
    }

    @RequestMapping("submitOrder")
    @LoginRequire
    public String submitOrder(HttpServletRequest request, OrderInfo orderInfo){

        String userId = (String) request.getAttribute("userId");

        String tradeNo = request.getParameter("tradeNo");
        boolean flag = orderService.checkTradeCode(userId, tradeNo);
        if (!flag){
            request.setAttribute("errMsg","该页面已失效，请重新结算!");
            return "tradeFail";
        }

        //http://www.gware.com/hasStock?skuId=10221&num=2

        String skuId = null;
        Integer num = null;
        String skuName = null;
        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            skuName = orderDetail.getSkuName();
            skuId = orderDetail.getSkuId();
            num = orderDetail.getSkuNum();
            try {
                String s = HttpClientUtils.get("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + num);
                if ("0".equals(s)){
                    request.setAttribute("errMsg",skuName + "库存不足！");
                    return "tradeFail";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            SkuInfo skuInfo = baseService.getSkuInfoBySkuId(skuId);
            orderDetail.setOrderPrice(skuInfo.getPrice());
        }

        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);

        String orderId = orderService.saveOrderInfoAndDetails(orderInfo);

        orderService.delTradeNo(userId);

        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }
}
