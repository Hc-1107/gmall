package com.dcs.gmall.service;

import com.dcs.gmall.bean.OrderInfo;
import com.dcs.gmall.bean.enums.ProcessStatus;

public interface OrderService {
    /**
     * 保存订单和订单详情
     * @param orderInfo
     * @return
     */
    String saveOrderInfoAndDetails(OrderInfo orderInfo);

    String getTradeNo(String userId);

    boolean checkTradeCode(String userId, String tradeNo);

    void delTradeNo(String userId);

    /**
     * 通过主键查询orderInfo
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfoById(String orderId);

    /**
     * 通过支付结果
     * @param orderId
     */
    void updateOrderInfoByPayResult(String orderId, String result, ProcessStatus processStatus);

    /**
     * 生产订单状态信息到消息队列
     * @param orderId
     */
    void produceMessageOrderStatus(String orderId);
}
