package com.dcs.gmall.service;

import com.dcs.gmall.bean.UserAddress;
import com.dcs.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {
    List<UserInfo> listAllUserInfo();

    List<UserAddress> getUserAddressById(String userId);

    /**
     * 通过用户登录名和密码登录
     * @param userInfo
     */
    UserInfo loginByLoginNameAndPasswd(UserInfo userInfo);

    /**
     * 通过用户id查询用户地址列表
     * @param userId
     */
    List<UserAddress> listUserAddressesByUserId(String userId);
}
