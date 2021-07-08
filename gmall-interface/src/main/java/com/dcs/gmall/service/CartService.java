package com.dcs.gmall.service;

import com.dcs.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {
    /**
     * 当用户登录时向购物车中添加数据
     * @param userId
     * @param skuId
     * @param skuNum
     */
    CartInfo addToCartLogin(String userId, String skuId, String skuNum);

    /**
     * 通过userId从缓存或者数据库中查询该用户的购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> cartListLogin(String userId);

    /**
     * 合并未登录时添加的购物车数据到用户的购物车数据
     * @param userId
     * @param cartInfoList
     */
    List<CartInfo> cartListLoginMerge(String userId, List<CartInfo> cartInfoList);

    /**
     * 登录状态下，将选中或未选中状态修改到redis缓存
     * @param userId
     * @param skuId
     * @param isChecked
     */
    void ckeckCartLogin(String userId, String skuId, String isChecked);

    List<CartInfo> getCartCheckedList(String userId);
}
