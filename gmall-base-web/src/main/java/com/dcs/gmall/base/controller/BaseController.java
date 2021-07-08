package com.dcs.gmall.base.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dcs.gmall.annotation.LoginRequire;
import com.dcs.gmall.bean.*;
import com.dcs.gmall.service.BaseService;
import com.dcs.gmall.service.ListService;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@CrossOrigin
public class BaseController {

    @Reference
    private BaseService baseService;

    @Reference
    private ListService listService;

    @Value("${fastDFS.ip}")
    private String fdfsIp;

    /**
     * 将sku上架
     */
    @RequestMapping("onSale")
    @ResponseBody
    @LoginRequire
    public void onSaleSku(String skuId){

        SkuInfo skuInfo = baseService.getSkuInfoBySkuId(skuId);
        SkuLsInfo skuLsInfo = new SkuLsInfo();

        BeanUtils.copyProperties(skuInfo, skuLsInfo);

        listService.saveSkuLsInfoES(skuLsInfo);

    }

    /**
     * 添加sku
     * @param skuInfo
     */
    @RequestMapping("saveSkuInfo")
    @ResponseBody
    @LoginRequire
    public void saveSkuInfo(@RequestBody SkuInfo skuInfo){
        baseService.saveSkuInfo(skuInfo);
    }

    /**
     * 查询spu所有的销售属性及销售属性值
     * @param spuId
     * @return
     */
    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    @LoginRequire
    public List<SpuSaleAttr> listSpuSaleAttrs(String spuId){

        List<SpuSaleAttr> list = baseService.listSpuSaleAttrs(spuId);

        return list;
    }

    /**
     * 查询spu的图片
     * @param spuId
     * @return
     */
    @RequestMapping("spuImageList")
    @ResponseBody
    @LoginRequire
    public List<SpuImage> listSpuImages(String spuId){
        List<SpuImage> list = baseService.listSpuImages(spuId);

        return list;
    }

    /**
     * 生成spu
     * @param spuInfo
     */
    @RequestMapping("/saveSpuInfo")
    @ResponseBody
    @LoginRequire
    public void saveSpuInfo(@RequestBody SpuInfo spuInfo){

        baseService.saveSpuInfo(spuInfo);

    }

    /**
     * 查询所有基本销售属性
     * @return
     */
    @RequestMapping("/baseSaleAttrList")
    @ResponseBody
    @LoginRequire
    public List<BaseSaleAttr> listBaseSaleAttrs(){

        List<BaseSaleAttr> list = baseService.listBaseSaleAttrs();

        return list;
    }

    //上传图片到fastDFS的storage_server
    @RequestMapping("/fileUpload")
    @ResponseBody
    @LoginRequire
    public String fileUploadFastDFS(MultipartFile file){

        String imgUrl = fdfsIp;
        try {
            String trackerConf = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(trackerConf);
            TrackerClient trackerClient=new TrackerClient();
            TrackerServer trackerServer=trackerClient.getTrackerServer();
            StorageClient storageClient=new StorageClient(trackerServer,null);
            //String orginalFilename="e://victor.jpg";
            String filename = file.getOriginalFilename();
            String extName = StringUtils.substringAfterLast(filename,".");
            String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
            for (int i = 0; i < upload_file.length; i++) {
                String path = upload_file[i];

                imgUrl += "/" + path;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        return imgUrl;
    }

    @RequestMapping("/getCatalog1")
    @ResponseBody
    @LoginRequire
    public List<BaseCatalog1> listCataLog1s(){

        List<BaseCatalog1> catalog1s = baseService.listCataLog1s();

        return catalog1s;
    }

    @RequestMapping("/getCatalog2")
    @ResponseBody
    @LoginRequire
    public List<BaseCatalog2> listCataLog2s(String catalog1Id){

        List<BaseCatalog2> catalog2s = baseService.listCataLog2s(catalog1Id);

        return catalog2s;
    }

    @RequestMapping("/getCatalog3")
    @ResponseBody
    @LoginRequire
    public List<BaseCatalog3> listCataLog3s(String catalog2Id){

        List<BaseCatalog3> catalog3s = baseService.listCataLog3s(catalog2Id);

        return catalog3s;
    }

    /**
     * 查询所有平台属性及平台属性值
     * @param catalog3Id
     * @return
     */
    @RequestMapping("/attrInfoList")
    @ResponseBody
    @LoginRequire
    public List<BaseAttrInfo> listAttrInfos(String catalog3Id){

        List<BaseAttrInfo> attrInfos = baseService.listAttrInfos(catalog3Id);

        return attrInfos;
    }

    @RequestMapping("/saveAttrInfo")
    @ResponseBody
    @LoginRequire
    public void saveAttrInfo(@RequestBody BaseAttrInfo attrInfo){

        baseService.saveAttrInfo(attrInfo);

    }

    @RequestMapping("/getAttrValueList")
    @ResponseBody
    @LoginRequire
    public List<BaseAttrValue> getAttrValueList(String attrId){

        List<BaseAttrValue> attrValueList = baseService.getAttrValueList(attrId);

        return attrValueList;
    }

    @RequestMapping("/spuList")
    @ResponseBody
    @LoginRequire
    public List<SpuInfo> getSpuinfoList(String catalog3Id){

        List<SpuInfo> spuInfoList = baseService.getSpuinfoList(catalog3Id);

        return spuInfoList;
    }

}
