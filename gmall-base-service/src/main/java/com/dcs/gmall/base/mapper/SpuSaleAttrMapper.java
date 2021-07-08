package com.dcs.gmall.base.mapper;

import com.dcs.gmall.bean.SpuSaleAttr;
import org.springframework.web.bind.annotation.RequestParam;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    List<SpuSaleAttr> listSpuSaleAttrs(String spuId);

    List<SpuSaleAttr> listSaleAttrAndValueAndCheckedBySkuIdAndSpuId(String skuId, String spuId);
}
