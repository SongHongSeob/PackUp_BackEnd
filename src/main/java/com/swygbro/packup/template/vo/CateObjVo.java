package com.swygbro.packup.template.vo;

import com.swygbro.packup.common.vo.CommonVo;

import lombok.Data;

@Data
public class CateObjVo extends CommonVo{
    private int objNo;
    private int cateNo;
    private String objNm;
}
