package com.dcs.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dcs.gmall.annotation.LoginRequire;
import com.dcs.gmall.bean.SkuInfo;
import com.dcs.gmall.bean.SpuSaleAttr;
import com.dcs.gmall.service.BaseService;
import com.dcs.gmall.service.ListService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@CrossOrigin
public class ItemController {

    @Reference
    private BaseService baseService;

    @Reference
    private ListService listService;

    /**
     * 请求商品详情数据
     * @return 商品详情页的静态页面模板
     */
    @RequestMapping("{skuId}.html")
    public String listSkuDetails(@PathVariable String skuId, HttpServletRequest request){

        //查询sku基本信息
        SkuInfo skuInfo = baseService.getSkuInfoBySkuId(skuId);

        request.setAttribute("skuInfo", skuInfo);

        String spuId = skuInfo.getSpuId();

        //通过skuId和spuId查询sku及其所属的spu的销售属性和销售属性值
        List<SpuSaleAttr> spuSaleAttrs = baseService.listSaleAttrAndValueAndCheckedBySkuIdAndSpuId(skuId, spuId);

        request.setAttribute("spuSaleAttrs", spuSaleAttrs);

        //查询当前spu对应的valuesSkuJson
        String valuesSkuJson = baseService.getValuesSkuJsonBySpuId(spuId);

        request.setAttribute("valuesSkuJson", valuesSkuJson);

        listService.incryHotScore(skuId);

        return "item";
    }
}
