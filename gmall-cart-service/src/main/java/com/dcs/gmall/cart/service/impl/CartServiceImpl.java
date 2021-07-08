package com.dcs.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.dcs.gmall.bean.CartInfo;
import com.dcs.gmall.bean.SkuInfo;
import com.dcs.gmall.cart.constant.CartConstant;
import com.dcs.gmall.cart.mapper.CartInfoMapper;
import com.dcs.gmall.service.BaseService;
import com.dcs.gmall.service.CartService;
import com.dcs.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Reference
    private BaseService baseService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public CartInfo addToCartLogin(String userId, String skuId, String skuNum) {

        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        cartInfo.setSkuId(skuId);

        cartInfo = cartInfoMapper.selectOne(cartInfo);

        if (cartInfo == null){

            SkuInfo skuInfo = new SkuInfo();
            skuInfo.setId(skuId);
            skuInfo = baseService.getSkuInfoBySkuId(skuId);

            cartInfo = new CartInfo();

            cartInfo.setSkuId(skuId);
            cartInfo.setUserId(userId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(Integer.parseInt(skuNum));

            cartInfoMapper.insertSelective(cartInfo);

        }else {

            cartInfo.setSkuNum(cartInfo.getSkuNum() + Integer.parseInt(skuNum));

            cartInfo.setSkuPrice(cartInfo.getCartPrice());

            cartInfoMapper.updateByPrimaryKeySelective(cartInfo);

        }
        //更新redis缓存
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();

            String userCartKey = CartConstant.USER_KEY_PREFIX + userId + CartConstant.USER_CART_KEY_SUFFIX;
            //hsah
            jedis.hset(userCartKey, skuId, JSON.toJSONString(cartInfo));

            //同步购物车和用户信息过期时间
            //获取key实时过期时间
            Long ttl = jedis.ttl(CartConstant.USER_KEY_PREFIX + userId + CartConstant.USERINFOKEY_SUFFIX);
            //设置hash key过期时间
            jedis.expire(userCartKey, ttl.intValue());


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null){
                jedis.close();
            }
        }


        return cartInfo;
    }

    @Override
    public List<CartInfo> cartListLogin(String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();

        Jedis jedis = redisUtil.getJedis();

        String userCartKey = CartConstant.USER_KEY_PREFIX + userId + CartConstant.USER_CART_KEY_SUFFIX;
        List<String> cartInfos = jedis.hvals(userCartKey);

        if (cartInfos!=null && cartInfos.size()>0){
            for (String cartStr : cartInfos) {
                cartInfoList.add(JSON.parseObject(cartStr, CartInfo.class));
            }
        }else {
            cartInfoList = cartInfoMapper.selectRealCartInfoListByUserId(userId);

            if (cartInfoList!=null && cartInfoList.size()>0){
                Map<String, String> map = new HashMap<>();
                for (CartInfo cartInfo : cartInfoList) {
                    map.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
                }

                jedis.hmset(userCartKey, map);

                Long ttl = jedis.ttl(CartConstant.USER_KEY_PREFIX + userId + CartConstant.USERINFOKEY_SUFFIX);
                jedis.expire(userCartKey, ttl.intValue());
            }
        }

        jedis.close();

        if (cartInfoList!=null && cartInfoList.size()>0){
            //Comparator compare需要指定泛型
            cartInfoList.sort((CartInfo o1, CartInfo o2) -> o2.getId().compareTo(o1.getId()));
        }

        return cartInfoList;
    }

    @Override
    @Transactional
    public List<CartInfo> cartListLoginMerge(String userId, List<CartInfo> cartInfoListCK) {
        List<CartInfo> cartInfoList = new ArrayList<>();

        Jedis jedis = redisUtil.getJedis();

        String userCartKey = CartConstant.USER_KEY_PREFIX + userId + CartConstant.USER_CART_KEY_SUFFIX;
        String cartCheckKey = CartConstant.USER_KEY_PREFIX + userId + CartConstant.USER_CHECKED_KEY_SUFFIX;
        List<String> cartInfos = jedis.hvals(userCartKey);

        if (cartInfos!=null && cartInfos.size()>0){
            for (String cartStr : cartInfos) {
                cartInfoList.add(JSON.parseObject(cartStr, CartInfo.class));
            }
        }else {
            cartInfoList = cartInfoMapper.selectRealCartInfoListByUserId(userId);
        }
        //未登录状态下加了购物车，在登录状态下没加购物车或者直接去结算的情况
        if (cartInfoList == null || cartInfoList.size()==0){
            for (CartInfo cartInfo : cartInfoListCK) {
                cartInfo.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfo);
            }
            cartInfoList = cartInfoListCK;
        }else {
            List<CartInfo> cartInfoListFinal = new ArrayList<>();
            for (CartInfo cartInfoDB : cartInfoList) {
                for (Iterator<CartInfo> iterator = cartInfoListCK.iterator(); iterator.hasNext(); ) {
                    CartInfo cartInfoCK =  iterator.next();
                    if (cartInfoCK.getSkuId().equals(cartInfoDB.getSkuId())){
                        cartInfoDB.setSkuNum(cartInfoDB.getSkuNum() + cartInfoCK.getSkuNum());
                        if ("1".equals(cartInfoCK.getIsChecked())){
                            cartInfoDB.setIsChecked(cartInfoCK.getIsChecked());
                        }
                        cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                        iterator.remove();
                    }
                }
                cartInfoListFinal.add(cartInfoDB);
            }

            if (cartInfoListCK!=null && cartInfoListCK.size()>0){
                for (CartInfo cartInfo : cartInfoListCK) {
                    cartInfo.setUserId(userId);
                    cartInfoMapper.insertSelective(cartInfo);
                    cartInfoListFinal.add(cartInfo);
                }
            }

            cartInfoList = cartInfoListFinal;
        }

        //同步redis缓存
        Map<String, String> map = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {
            map.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
            saveCartIfChecked(jedis, cartCheckKey, cartInfo);
        }

        jedis.hmset(userCartKey,map);

        Long ttl = jedis.ttl(CartConstant.USER_KEY_PREFIX + userId + CartConstant.USERINFOKEY_SUFFIX);
        jedis.expire(userCartKey, ttl.intValue());
        jedis.expire(cartCheckKey,ttl.intValue());

        jedis.close();

        //排序
        cartInfoList.sort((CartInfo o1, CartInfo o2) -> o2.getId().compareTo(o1.getId()));

        return cartInfoList;
    }

    private void saveCartIfChecked(Jedis jedis, String cartCheckKey, CartInfo cartInfo) {
        if ("1".equals(cartInfo.getIsChecked())){
            jedis.hset(cartCheckKey, cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
        }
    }

    @Override
    public void ckeckCartLogin(String userId, String skuId, String isChecked) {

        Jedis jedis = redisUtil.getJedis();

        String userCartKey = CartConstant.USER_KEY_PREFIX + userId + CartConstant.USER_CART_KEY_SUFFIX;

        String cartInfoStr = jedis.hget(userCartKey, skuId);

        CartInfo cartInfo = JSON.parseObject(cartInfoStr, CartInfo.class);

        cartInfo.setIsChecked(isChecked);

        jedis.hset(userCartKey, skuId, JSON.toJSONString(cartInfo));

        String cartCkeckedKey = CartConstant.USER_KEY_PREFIX + userId + CartConstant.USER_CHECKED_KEY_SUFFIX;

        if ("1".equals(isChecked)){
            jedis.hset(cartCkeckedKey, skuId, JSON.toJSONString(cartInfo));
        }else {
            jedis.hdel(cartCkeckedKey, skuId);

        }
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        String userCheckedKey = CartConstant.USER_KEY_PREFIX + userId + CartConstant.USER_CHECKED_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        List<String> cartCheckedList = jedis.hvals(userCheckedKey);
        List<CartInfo> newCartList = new ArrayList<>();
        for (String cartJson : cartCheckedList) {
            CartInfo cartInfo = JSON.parseObject(cartJson,CartInfo.class);
            newCartList.add(cartInfo);
        }
        return newCartList;
    }

}
