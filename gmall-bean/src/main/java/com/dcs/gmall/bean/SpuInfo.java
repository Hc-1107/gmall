package com.dcs.gmall.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
public class SpuInfo implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Id
    @Column
    private String spuName;

    @Id
    @Column
    private String description;

    @Id
    @Column
    private String catalog3Id;

    @Id
    @Column
    private String tmId;

    @Transient
    private List<SpuSaleAttr> spuSaleAttrList;

    @Transient
    private List<SpuImage> spuImageList;

}
