package com.dcs.gmall.cart.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.dcs.gmall.bean.CartInfo;
import com.dcs.gmall.bean.SkuInfo;
import com.dcs.gmall.service.BaseService;
import com.dcs.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class CookieHandler {

    @Reference
    private BaseService baseService;

    private static final int COOKIE_MAX_AGE = 30*24*60*60;

    /**
     * 用户未登录添加购物车
     * @param request
     * @param response
     * @param skuId
     * @param skuNum
     */
    public void addTOCartLogout(HttpServletRequest request, HttpServletResponse response, String skuId, String skuNum) {

        boolean exist = false;

        CartInfo cartInfo = new CartInfo();

        String userCartInfo = CookieUtil.getCookieValue(request, "userCartInfo", true);

        List<CartInfo> cartInfos = new ArrayList<>();
        if (StringUtils.isNotEmpty(userCartInfo)) {

            cartInfos = JSON.parseArray(userCartInfo, CartInfo.class);

            for (Iterator<CartInfo> iterator = cartInfos.iterator(); iterator.hasNext(); ) {
                CartInfo cart = iterator.next();

                if (skuId.equals(cart.getSkuId())){
                    cartInfo = cart;
                    exist = true;
                    iterator.remove();
                }

            }
        }


        if (!exist){
            SkuInfo skuInfo = baseService.getSkuInfoBySkuId(skuId);

            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(Integer.parseInt(skuNum));
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuId(skuId);

            cartInfos.add(cartInfo);

        }else {

            cartInfo.setSkuNum(Integer.parseInt(skuNum) + 1);
            cartInfo.setSkuPrice(cartInfo.getCartPrice());

            cartInfos.add(cartInfo);
        }

        CookieUtil.setCookie(request, response, "userCartInfo", JSON.toJSONString(cartInfos),COOKIE_MAX_AGE,true);

        request.setAttribute("cartInfo", cartInfo);
    }

    public List<CartInfo> cartListUnLogin(HttpServletRequest request) {

        List<CartInfo> cartInfoList = null;

        String userCartInfo = CookieUtil.getCookieValue(request, "userCartInfo", true);

        if (userCartInfo != null){
            cartInfoList = JSON.parseArray(userCartInfo, CartInfo.class);
        }

        return cartInfoList;
    }

    public void deleteCartInfo(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request,response,"userCartInfo");
    }

    /**
     * 未登录状态下，保存cartInfo中商品的选中或未选中状态
     * @param request
     * @param response
     * @param skuId
     * @param isChecked
     */
    public void checkCartUnLogin(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {

        List<CartInfo> cartInfoList = cartListUnLogin(request);

        for (CartInfo cartInfo : cartInfoList) {

            if (cartInfo.getSkuId().equals(skuId)){
                cartInfo.setIsChecked(isChecked);
            }
        }

        CookieUtil.setCookie(request, response, "userCartInfo", JSON.toJSONString(cartInfoList), COOKIE_MAX_AGE, true);
    }
}
