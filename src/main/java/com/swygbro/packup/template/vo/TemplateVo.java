package com.swygbro.packup.template.vo;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swygbro.packup.common.vo.CommonVo;

import lombok.Data;

@Data
public class TemplateVo extends CommonVo{
    private int templateNo;
    private int userNo;
    private String templateNm;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul") // M, d는 1-2자리 허용
    private Date alarmDt;                           // 알람 요청 일자 및 시간, REPEAT_TYPE이 단발성(FALSE)일때 저장
    private Boolean repeatType;                     // 반복(TRUE), 단발성(FALSE)
    private String alarmRepeatDay;                  // 반복 알람일(예시 월,화,수...), REPEAT_TYPE이 반복(TRUE)일 때만 저장
    private String alarmTime;                         // 알람 요청 시간, REPEAT_TYPE이 단발성(FALSE)일때 저장
    
    private String isFavorite;						// 즐겨찾기 체크용('Y' , 'N')
    private String cateNm;
    private Integer cateNo;
    private int step;
    private String alarmText;
    private String slackYn;
    private String googleCalendarYn;

    List<TempStepVo> stepsList;
}