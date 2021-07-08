package com.dcs.gmall.base.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.dcs.gmall.base.constant.BaseContant;
import com.dcs.gmall.base.mapper.*;
import com.dcs.gmall.bean.*;
import com.dcs.gmall.exception.GmallException;
import com.dcs.gmall.service.BaseService;
import com.dcs.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class BaseServiceImpl implements BaseService {

    @Autowired
    private Catalog1Mapper catalog1Mapper;

    @Autowired
    private Catalog2Mapper catalog2Mapper;

    @Autowired
    private Catalog3Mapper catalog3Mapper;

    @Autowired
    private AttrInfoMapper attrInfoMapper;

    @Autowired
    private AttrValueMapper attrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;

    private Lock lock = new ReentrantLock();

    @Override
    public List<BaseCatalog1> listCataLog1s() {

        List<BaseCatalog1> catalog1s = catalog1Mapper.selectAll();

        return catalog1s;
    }

    @Override
    public List<BaseCatalog2> listCataLog2s(String catalog1Id) {

        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);

        List<BaseCatalog2> baseCatalog2s = catalog2Mapper.select(baseCatalog2);

        return baseCatalog2s;
    }

    @Override
    public List<BaseCatalog3> listCataLog3s(String catalog2Id) {

        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);

        List<BaseCatalog3> baseCatalog3s = catalog3Mapper.select(baseCatalog3);

        return baseCatalog3s;
    }

    @Override
    public List<BaseAttrInfo> listAttrInfos(String catalog3Id) {

        List<BaseAttrInfo> attrInfos = attrInfoMapper.listAttrInfos(catalog3Id);

        return attrInfos;
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo attrInfo) {

        String attrId = attrInfo.getId();

        if (StringUtils.isEmpty(attrId)) {
            attrInfoMapper.insert(attrInfo);
        }else {
            attrInfoMapper.updateByPrimaryKey(attrInfo);

            BaseAttrValue attrValue = new BaseAttrValue();
            attrValue.setAttrId(attrId);
            attrValueMapper.delete(attrValue);
        }

        List<BaseAttrValue> attrValueList = attrInfo.getAttrValueList();
        if (attrValueList != null) {
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(attrId);
                attrValueMapper.insert(baseAttrValue);
            }
        }else {
            throw new GmallException("平台属性值为空，请输入平台属性值！", 4000);
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {

        BaseAttrValue attrValue = new BaseAttrValue();
        attrValue.setAttrId(attrId);

        List<BaseAttrValue> attrValueList = attrValueMapper.select(attrValue);

        return attrValueList;
    }

    @Override
    public List<SpuInfo> getSpuinfoList(String catalog3Id) {

        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);

        List<SpuInfo> spuInfoList = spuInfoMapper.select(spuInfo);

        return spuInfoList;
    }

    @Override
    public List<BaseSaleAttr> listBaseSaleAttrs() {

        List<BaseSaleAttr> list = baseSaleAttrMapper.selectAll();

        return list;
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {

        spuInfoMapper.insertSelective(spuInfo);
        String spuInfoId = spuInfo.getId();

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfoId);
            spuImageMapper.insertSelective(spuImage);
        }

        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuInfoId);
            spuSaleAttrMapper.insertSelective(spuSaleAttr);

            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(spuInfoId);
                spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
            }
        }

    }

    @Override
    public List<SpuImage> listSpuImages(String spuId) {

        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        List<SpuImage> list = spuImageMapper.select(spuImage);

        return list;
    }

    @Override
    public List<SpuSaleAttr> listSpuSaleAttrs(String spuId) {

        List<SpuSaleAttr> list = spuSaleAttrMapper.listSpuSaleAttrs(spuId);

        return list;
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {

        if (skuInfo!=null){
            skuInfoMapper.insertSelective(skuInfo);
        }else {
            throw new GmallException("请填写sku信息!",40000);
        }

        String skuInfoId = skuInfo.getId();

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if(skuImageList!=null && skuImageList.size()>0){
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfoId);
                skuImageMapper.insertSelective(skuImage);
            }
        }else {
            throw new GmallException("请勾选sku的图片!",40000);
        }

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if(skuAttrValueList!=null && skuAttrValueList.size()>0){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfoId);
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }else {
            throw new GmallException("请选择sku的平台属性值!",40000);
        }

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if(skuSaleAttrValueList!=null && skuSaleAttrValueList.size()>0){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfoId);
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }else {
            throw new GmallException("请选择sku的销售属性值!",40000);
        }

    }

    @Override
    public SkuInfo getSkuInfoBySkuId(String skuId) {
        //由于redis集群可能会宕机，所以要加try/catch/finally
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();

            String skuKey = BaseContant.SKUKEY_PREFIX + skuId + BaseContant.SKUKEY_SUFFIX;

            if(jedis.exists(skuKey)){
                String skuInfoString = jedis.get(skuKey);
                return JSON.parseObject(skuInfoString,SkuInfo.class);
            }else{
                //由于redis可能会出现在key无效时大并发都从数据库取数据造成数据库压力过大(缓存击穿),所以要加锁让一个线程先从数据库取再存入redis
                String lockKey = BaseContant.SKUKEY_PREFIX + skuId + BaseContant.SKUKEY_LOCK;

                String lockflag = jedis.set(lockKey, "lock", "NX", "PX", BaseContant.SKULOCK_TIMEOUT);
                //返回ok代表该线程抢到了redis分布式锁

                if ("OK".equals(lockflag) && !jedis.exists(skuKey)) {

                    SkuInfo skuInfo = getSkuInfoDB(skuId);
                    //由于redis可能会出现缓存穿透问题，所以从数据库查询数据无论存不存在都要存入redis
                    //由于redis可能会出现缓存雪崩问题，所以各个业务设置key失效时间都要不相同避免同一时间集体失效
                    jedis.setex(skuKey, BaseContant.SKUKEY_TIMEOUT,JSON.toJSONString(skuInfo));

                    jedis.del(lockKey);

                    return skuInfo;
                } else {
                    //没有抢到redis分布式锁的线程
                    Thread.sleep(1000);
                    getSkuInfoBySkuId(skuId);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null){
                jedis.close();
            }
        }
        //如果redis服务宕机,从数据库返回数据
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoDB(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);

        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);

        skuInfo.setSkuImageList(skuImageList);

        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);

        skuInfo.setSkuAttrValueList(skuAttrValueList);

        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> listSaleAttrAndValueAndCheckedBySkuIdAndSpuId(String skuId, String spuId) {

        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.listSaleAttrAndValueAndCheckedBySkuIdAndSpuId(skuId, spuId);

        return spuSaleAttrs;
    }

    @Override
    public String getValuesSkuJsonBySpuId(String spuId) {

        List<SkuSaleAttrValue> list = skuSaleAttrValueMapper.selectValuesSkuBySpuId(spuId);

        //将list转化为valuesSkuMap {"180|190":"33"}
        Map<String, String> valuesSkuMap = new HashMap<>();
        String key = "";

        for (int i = 0; i <= list.size(); i++) {
            if(i == list.size()){
                valuesSkuMap.put(key,list.get(i-1).getSkuId());
                continue;
            }

            SkuSaleAttrValue value = list.get(i);

            if(i==0){
                key = value.getSaleAttrValueId();
                continue;
            }

            if(value.getSkuId().equals(list.get(i-1).getSkuId())){
                key += "|" + value.getSaleAttrValueId();
            }else{
                valuesSkuMap.put(key,list.get(i-1).getSkuId());
                key = value.getSaleAttrValueId();
            }
        }

        String valuesSkuJson = JSON.toJSONString(valuesSkuMap);

        return valuesSkuJson;
    }

    @Override
    public List<BaseAttrInfo> listAttrInfos(List<String> attrValueIdList) {

        String param = StringUtils.join(attrValueIdList.toArray(), ",");

        List<BaseAttrInfo> attrInfoList = null;
        if (param != null && param.length() > 0) {
            attrInfoList = attrInfoMapper.listAttrInfosbyValueIds(param);
        }
        return attrInfoList;
    }

}
