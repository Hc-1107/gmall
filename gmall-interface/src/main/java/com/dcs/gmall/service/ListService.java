package com.dcs.gmall.service;

import com.dcs.gmall.bean.SkuLsInfo;
import com.dcs.gmall.bean.SkuLsParams;
import com.dcs.gmall.bean.SkuLsResult;

public interface ListService {
    /**
     * 将skuLsInfo存入elastic search
     * @param skuLsInfo
     */
    void saveSkuLsInfoES(SkuLsInfo skuLsInfo);

    /**
     * 通过关键字/三级分类id/平台属性值id在elastic search中检索sku信息
     * @param skuLsParams
     * @return
     */
    SkuLsResult searchSkusES(SkuLsParams skuLsParams);

    /**
     * 修改es中的skuInfo的hotScore+1
     * @param skuId
     */
    void incryHotScore(String skuId);
}
