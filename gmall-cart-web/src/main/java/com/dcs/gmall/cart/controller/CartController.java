package com.dcs.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dcs.gmall.annotation.LoginRequire;
import com.dcs.gmall.bean.CartInfo;
import com.dcs.gmall.cart.handler.CookieHandler;
import com.dcs.gmall.service.CartService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@CrossOrigin
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private CookieHandler cookieHandler;

    @RequestMapping("addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response){

        String userId = (String)request.getAttribute("userId");

        String skuNum = request.getParameter("num");
        String skuId = request.getParameter("skuId");

        if (userId != null && userId.length() > 0){
            //如果登录了，将cartInfo更新到mysql和redis
            CartInfo cartInfo = cartService.addToCartLogin(userId, skuId, skuNum);

            request.setAttribute("cartInfo", cartInfo);

        }else {
            cookieHandler.addTOCartLogout(request, response, skuId, skuNum);
        }
        return "success";
    }

    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request, HttpServletResponse response){

        String userId = (String)request.getAttribute("userId");

        List<CartInfo> cartInfoList = null;

        if (StringUtils.isEmpty(userId)){
            cartInfoList = cookieHandler.cartListUnLogin(request);
        }else {
            cartInfoList = cookieHandler.cartListUnLogin(request);
            if (cartInfoList == null || cartInfoList.size()==0){
                cartInfoList = cartService.cartListLogin(userId);
            }else {
                cartInfoList = cartService.cartListLoginMerge(userId, cartInfoList);
                cookieHandler.deleteCartInfo(request, response);
            }
        }

        request.setAttribute("cartInfoList", cartInfoList);

        return "cartList";
    }

    @RequestMapping("checkCart")
    @ResponseBody
    public void checkCart(HttpServletRequest request, HttpServletResponse response){

        String userId = (String) request.getAttribute("userId");
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");

        if (StringUtils.isEmpty(userId)){

            cookieHandler.checkCartUnLogin(request, response, skuId, isChecked);

        }else {

            cartService.ckeckCartLogin(userId, skuId, isChecked);

        }
    }

    @RequestMapping("toTrade")
    @LoginRequire
    public String toTrade(HttpServletRequest request, HttpServletResponse response){

        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartList = cookieHandler.cartListUnLogin(request);
        if (cartList!=null && cartList.size()>0){
            cartService.cartListLoginMerge(userId, cartList);
            cookieHandler.deleteCartInfo(request,response);
        }

        return "redirect://order.gmall.com/trade";
    }
}
