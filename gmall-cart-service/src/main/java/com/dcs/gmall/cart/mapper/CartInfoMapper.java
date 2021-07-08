package com.dcs.gmall.cart.mapper;

import com.dcs.gmall.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {
    List<CartInfo> selectRealCartInfoListByUserId(String userId);
}
