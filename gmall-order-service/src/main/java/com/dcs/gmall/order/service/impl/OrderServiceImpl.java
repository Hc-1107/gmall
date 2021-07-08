package com.dcs.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.dcs.gmall.bean.OrderDetail;
import com.dcs.gmall.bean.OrderInfo;
import com.dcs.gmall.bean.enums.ProcessStatus;
import com.dcs.gmall.order.mapper.OrderDetailMapper;
import com.dcs.gmall.order.mapper.OrderInfoMapper;
import com.dcs.gmall.service.OrderService;
import com.dcs.gmall.util.ActiveMQUtil;
import com.dcs.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Override
    @Transactional
    public String saveOrderInfoAndDetails(OrderInfo orderInfo) {

        // 设置创建时间
        orderInfo.setCreateTime(new Date());
        // 设置失效时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        // 生成第三方支付编号
        String outTradeNo="DCS"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);

        orderInfoMapper.insertSelective(orderInfo);

        String id = orderInfo.getId();

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {

            orderDetail.setOrderId(id);
            orderDetailMapper.insertSelective(orderDetail);

        }

        return id;
    }

    // 生成流水号
    @Override
    public  String getTradeNo(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeCode";
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeNoKey,10*60,tradeCode);
        jedis.close();
        return tradeCode;
    }

    // 验证流水号
    @Override
    public  boolean checkTradeCode(String userId,String tradeCodeNo){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:"+userId+":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);
        jedis.close();
        if (tradeCode!=null && tradeCode.equals(tradeCodeNo)){
            return  true;
        }else{
            return false;
        }
    }

    // 删除流水号
    @Override
    public void  delTradeNo(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey =  "user:"+userId+":tradeCode";
        jedis.del(tradeNoKey);
        jedis.close();
    }

    @Override
    public OrderInfo getOrderInfoById(String orderId) {

        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);

        orderInfo.setOrderDetailList(orderDetailList);

        return orderInfo;
    }

    @Override
    public void updateOrderInfoByPayResult(String orderId, String result, ProcessStatus processStatus) {

        if ("success".equals(result)){

            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setId(orderId);
            orderInfo.setProcessStatus(processStatus);
            orderInfo.setOrderStatus(processStatus.getOrderStatus());

            orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
        }

    }

    @Override
    public void produceMessageOrderStatus(String orderId) {

        String message = createMessageForWareService(orderId);

        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();

            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");

            MessageProducer producer = session.createProducer(order_result_queue);

            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();

            textMessage.setText(message);

            producer.send(textMessage);

            session.commit();

            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    private String createMessageForWareService(String orderId) {

        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);

        OrderDetail orderDe = new OrderDetail();
        orderDe.setOrderId(orderId);
        List<OrderDetail> select = orderDetailMapper.select(orderDe);
        orderInfo.setOrderDetailList(select);

        Map<String,Object> map = new HashMap<>();
        map.put("orderId",orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody",orderInfo.getTradeBody());
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");
        map.put("wareId",orderInfo.getWareId());

        // 组合json
        List detailList = new ArrayList();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            Map detailMap = new HashMap();
            detailMap.put("skuId",orderDetail.getSkuId());
            detailMap.put("skuName",orderDetail.getSkuName());
            detailMap.put("skuNum",orderDetail.getSkuNum());
            detailList.add(detailMap);
        }
        map.put("details",detailList);

        return JSON.toJSONString(map);
    }


}
