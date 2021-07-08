package com.dcs.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.dcs.gmall.bean.PaymentInfo;
import com.dcs.gmall.payment.mapper.PaymentInfoMapper;
import com.dcs.gmall.service.PaymentSerivce;
import com.dcs.gmall.util.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;

@Service
public class PaymentServiceImpl implements PaymentSerivce {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {

        paymentInfoMapper.insertSelective(paymentInfo);



    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery) {
        return paymentInfoMapper.selectOne(paymentInfoQuery);
    }

    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUPD) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", out_trade_no);
        paymentInfoMapper.updateByExampleSelective(paymentInfoUPD, example);
    }

    @Override
    public boolean refund(String orderId) {
        //AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        //详见支付宝开放平台统一退款api所必需参数
        request.setBizContent("{" +
                "\"out_trade_no\":\"20150320010101001\"," +
                "\"trade_no\":\"2014112611001004680073956707\"," +
                "\"refund_amount\":200.12," +
                "\"refund_currency\":\"USD\"," +
                "\"refund_reason\":\"正常退款\"," +
                "\"out_request_no\":\"HZ01RF001\"," +
                "\"operator_id\":\"OP001\"," +
                "\"store_id\":\"NJ_S_001\"," +
                "\"terminal_id\":\"NJ_T_001\"," +
                "      \"goods_detail\":[{" +
                "        \"goods_id\":\"apple-01\"," +
                "\"alipay_goods_id\":\"20010001\"," +
                "\"goods_name\":\"ipad\"," +
                "\"quantity\":1," +
                "\"price\":2000," +
                "\"goods_category\":\"34543238\"," +
                "\"categories_tree\":\"124868003|126232002|126252004\"," +
                "\"body\":\"特价手机\"," +
                "\"show_url\":\"http://www.alipay.com/xxx.jpg\"" +
                "        }]," +
                "      \"refund_royalty_parameters\":[{" +
                "        \"royalty_type\":\"transfer\"," +
                "\"trans_out\":\"2088101126765726\"," +
                "\"trans_out_type\":\"userId\"," +
                "\"trans_in_type\":\"userId\"," +
                "\"trans_in\":\"2088101126708402\"," +
                "\"amount\":0.1," +
                "\"amount_percentage\":100," +
                "\"desc\":\"分账给2088101126708402\"" +
                "        }]," +
                "\"org_pid\":\"2088101117952222\"," +
                "      \"query_options\":[" +
                "        \"refund_detail_item_list\"" +
                "      ]" +
                "  }");
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void sendPayResult(String orderId, String result){

        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            Queue payment_result_queue = session.createQueue("PAYMENT_RESULT_QUEUE");

            MessageProducer producer = session.createProducer(payment_result_queue);

            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("orderId", orderId);
            mapMessage.setString("result", result);

            producer.send(mapMessage);

            session.commit();

            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
