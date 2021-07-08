package com.dcs.gmall.service;

import com.dcs.gmall.bean.*;

import java.util.List;

public interface BaseService {
    /**
     * 查询所有一级分类数据
     * @return
     */
    List<BaseCatalog1> listCataLog1s();

    /**
     * 通过一级分类id查询所有二级分类数据
     * @param catalog1Id
     * @return
     */
    List<BaseCatalog2> listCataLog2s(String catalog1Id);

    /**
     * 通过二级分类id查询所有三级分类数据
     * @param catalog2Id
     * @return
     */
    List<BaseCatalog3> listCataLog3s(String catalog2Id);

    /**
     * 通过三级分类id查询所有平台属性
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> listAttrInfos(String catalog3Id);

    /**
     * 保存平台属性和属性值到数据库
     * @param attrInfo
     */
    void saveAttrInfo(BaseAttrInfo attrInfo);

    /**
     * 通过平台属性id查询属性值列表
     * @param attrId
     * @return
     */
    List<BaseAttrValue> getAttrValueList(String attrId);

    /**
     * 通过第三级分类id查询spu列表
     * @param catalog3Id
     * @return
     */
    List<SpuInfo> getSpuinfoList(String catalog3Id);

    /**
     * 查询所有基本销售属性
     * @return
     */
    List<BaseSaleAttr> listBaseSaleAttrs();

    /**
     * 保存spu到数据库
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据spuId查询spu的图片列表
     * @param spuId
     * @return
     */
    List<SpuImage> listSpuImages(String spuId);

    /**
     * 根据spuId查询spu所有的销售属性及销售属性值
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> listSpuSaleAttrs(String spuId);

    /**
     * 将sku及其平台属性，销售属性，图片插入数据库
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 通过skuId查询skuInfo及其图片
     * @return
     * @param skuId
     */
    SkuInfo getSkuInfoBySkuId(String skuId);

    /**
     * 通过skuId和spuId查询spu销售属性及值和其值被sku销售属性值选中的checked
     * @param skuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> listSaleAttrAndValueAndCheckedBySkuIdAndSpuId(String skuId, String spuId);

    /**
     * 通过spuId查询出当前spu的valuesSkuJson
     * @param spuId
     * @return
     */
    String getValuesSkuJsonBySpuId(String spuId);

    /**
     * 通过attrValueId集合查询平台属性和属性值
     * @param attrValueIdList
     * @return
     */
    List<BaseAttrInfo> listAttrInfos(List<String> attrValueIdList);
}
