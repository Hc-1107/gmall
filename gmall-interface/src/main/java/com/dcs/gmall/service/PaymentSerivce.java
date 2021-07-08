package com.dcs.gmall.service;

import com.dcs.gmall.bean.PaymentInfo;

public interface PaymentSerivce {
    /**
     * 保存支付信息
     * @param paymentInfo
     */
    void savePaymentInfo(PaymentInfo paymentInfo);

    PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);

    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUPD);

    boolean refund(String orderId);

    /**
     * 将支付结果发送到消息队列
     * @param orderId
     * @param result
     */
    void sendPayResult(String orderId, String result);
}
