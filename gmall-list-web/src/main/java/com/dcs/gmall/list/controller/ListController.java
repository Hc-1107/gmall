package com.dcs.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.dcs.gmall.annotation.LoginRequire;
import com.dcs.gmall.bean.*;
import com.dcs.gmall.service.BaseService;
import com.dcs.gmall.service.ListService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {
    @Reference
    private ListService listService;

    @Reference
    private BaseService baseService;

    /**
     * 全文检索商品
     * @param skuLsParams
     * @return
     */
    @RequestMapping("list.html")
    public String searchSkus(SkuLsParams skuLsParams, HttpServletRequest request){
        String keyword = skuLsParams.getKeyword();
        if (keyword != null && keyword.length() > 0){
            request.setAttribute("keyword", keyword);
        }

        skuLsParams.setPageSize(2);

        SkuLsResult skuLsResult = listService.searchSkusES(skuLsParams);

        //通过valueIds查询baseAttrInfo
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> attrInfos = baseService.listAttrInfos(attrValueIdList);

        List<BaseAttrValue> crumbs = new ArrayList<>();
        for (Iterator<BaseAttrInfo> iterator = attrInfos.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo attrInfo = iterator.next();
            List<BaseAttrValue> attrValueList = attrInfo.getAttrValueList();
            for (BaseAttrValue attrValue : attrValueList) {
                String[] valueIds = skuLsParams.getValueId();
                if(valueIds!=null && valueIds.length>0){
                    for (String valueId : valueIds) {
                        if(valueId.equals(attrValue.getId())){
                            iterator.remove();

                            BaseAttrValue crumb = new BaseAttrValue();

                            crumb.setValueName(attrInfo.getAttrName()+":"+attrValue.getValueName());

                            String newUrlParam = makeUrlParams(skuLsParams, valueId);

                            crumb.setUrlParam(newUrlParam);

                            crumbs.add(crumb);
                        }
                    }
                }
            }
        }

        request.setAttribute("crumbs", crumbs);

        request.setAttribute("attrInfos", attrInfos);

        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();

        request.setAttribute("skuLsInfoList", skuLsInfoList);

        String urlParams = makeUrlParams(skuLsParams);

        request.setAttribute("urlParams", urlParams);

        request.setAttribute("totalPages", skuLsResult.getTotalPages());

        request.setAttribute("pageNo", skuLsParams.getPageNo());

        return "list";
    }

    private String makeUrlParams(SkuLsParams skuLsParams, String... excludeValueId) {
        String urlParams = "";

        String keyword = skuLsParams.getKeyword();
        if(keyword != null && keyword.length() > 0){
            urlParams += "keyword=" + keyword;
        }

        String catalog3Id = skuLsParams.getCatalog3Id();
        if(catalog3Id != null && catalog3Id.length() > 0){
            urlParams += "catalog3Id=" + catalog3Id;
        }

        String[] valueIds = skuLsParams.getValueId();
        if(valueIds != null && valueIds.length > 0){
            for (String valueId : valueIds) {
                if (excludeValueId!=null && excludeValueId.length>0){
                    if (excludeValueId[0].equals(valueId)){
                        continue;
                    }
                }
                urlParams += "&valueId=" + valueId;
            }
        }

        return  urlParams;
    }
}
