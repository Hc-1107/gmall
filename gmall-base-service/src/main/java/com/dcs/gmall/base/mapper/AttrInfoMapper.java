package com.dcs.gmall.base.mapper;

import com.dcs.gmall.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface AttrInfoMapper extends Mapper<BaseAttrInfo> {
    /**
     * 通过三级分类id联合查询attrInfo和attrValue
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> listAttrInfos(String catalog3Id);

    /**
     * 通过attrValueId List 联合查询 attrInfo List 和 attrValue List
     * @param attrValueIdList
     * @return
     */
    List<BaseAttrInfo> listAttrInfosbyValueIds(@Param("attrValueIdList") String attrValueIdList);
}
