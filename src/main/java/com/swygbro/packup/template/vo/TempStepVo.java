package com.swygbro.packup.template.vo;

import java.util.Date;
import java.util.List;

import com.swygbro.packup.common.vo.CommonVo;

import lombok.Data;

@Data
public class TempStepVo extends CommonVo{
    private int templateStepNo;                     // 유저 템플릿 스텝 번호
    private int templateNo;
    private int step;                               // 스텝 단계
    private Float stepX;                        // 스텝의 x 위치 값         
    private Float stepY;                        // 스텝의 y 위치 값

    // 배열 형태의 하위 객체들
    private List<TempStepObjVo> stepObjList;
    private List<TempStepTextVo> stepTextList;
}