package com.dcs.gmall.order.mq;

import com.dcs.gmall.bean.enums.ProcessStatus;
import com.dcs.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderConsumer {

    @Autowired
    private OrderService orderService;

    /**
     * 监听消息队列PAYMENT_RESULT_QUEUE并消费其消息
     */
    @JmsListener(destination = "PAYMENT_RESULT_QUEUE", containerFactory = "jmsQueueListener")
    public void consumePayResult(MapMessage mapMessage) throws JMSException {

        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");

        orderService.updateOrderInfoByPayResult(orderId, result, ProcessStatus.PAID);

        orderService.produceMessageOrderStatus(orderId);

        orderService.updateOrderInfoByPayResult(orderId, result, ProcessStatus.NOTIFIED_WARE);
    }

    @JmsListener(destination = "SKU_DEDUCT_QUEUE", containerFactory = "jmsQueueListener")
    public void consumeWareStatus(MapMessage mapMessage) throws JMSException {

        String orderId = mapMessage.getString("orderId");
        String status = mapMessage.getString("status");

        if ("DEDUCTED".equals(status)){

            orderService.updateOrderInfoByPayResult(orderId, "success", ProcessStatus.WAITING_DELEVER);

        }else {

            orderService.updateOrderInfoByPayResult(orderId, "success", ProcessStatus.STOCK_EXCEPTION);
        }

    }
}
