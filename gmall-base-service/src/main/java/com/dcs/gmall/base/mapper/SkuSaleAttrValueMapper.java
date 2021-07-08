package com.dcs.gmall.base.mapper;


import com.dcs.gmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    List<SkuSaleAttrValue> selectValuesSkuBySpuId(String spuId);
}
