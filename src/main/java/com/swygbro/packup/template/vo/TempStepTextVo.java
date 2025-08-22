package com.swygbro.packup.template.vo;

import com.swygbro.packup.common.vo.CommonVo;

import lombok.Data;

@Data
public class TempStepTextVo extends CommonVo{

    private int textNo;                     // 유저 텍스트 문구 번호
    private int templateStepNo;             // 템플릿 스텝 번호
    private String text;                       // 단계별 텍스트
    private Float stepTextX;                // 텍스트의 x 위치 값
    private Float stepTextY;                // 텍스트의 y 위치 값
    private int templateNo;                 // 템플릿 번호
    private int step;                       // 스텝
}