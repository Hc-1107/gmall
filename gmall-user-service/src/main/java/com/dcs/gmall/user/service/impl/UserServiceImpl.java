package com.dcs.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.dcs.gmall.bean.UserAddress;
import com.dcs.gmall.bean.UserInfo;
import com.dcs.gmall.service.UserService;
import com.dcs.gmall.user.mapper.UserAddressMapper;
import com.dcs.gmall.user.mapper.UserInfoMapper;
import com.dcs.gmall.util.RedisUtil;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import sun.security.provider.MD5;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private String userKey_prefix="user:";
    private String userinfoKey_suffix=":info";
    private int userKey_timeOut=30*24*60*60;


    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<UserInfo> listAllUserInfo() {
        return null;
    }

    @Override
    public List<UserAddress> getUserAddressById(String userId) {
        return null;
    }

    @Override
    public UserInfo loginByLoginNameAndPasswd(UserInfo userInfo) {

        String passwd = userInfo.getPasswd();
        String s = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(s);
        UserInfo user = userInfoMapper.selectOne(userInfo);

        if (user != null){

            try {
                Jedis jedis = redisUtil.getJedis();
                //user:userId:info
                String key = userKey_prefix + user.getId() + userinfoKey_suffix;
                jedis.setex(key, userKey_timeOut, JSON.toJSONString(user));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return user;
    }

    @Override
    public List<UserAddress> listUserAddressesByUserId(String userId) {

        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        List<UserAddress> userAddressList = userAddressMapper.select(userAddress);

        return userAddressList;
    }
}
