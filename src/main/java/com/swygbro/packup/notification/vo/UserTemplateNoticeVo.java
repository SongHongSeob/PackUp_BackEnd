package com.swygbro.packup.notification.vo;

import lombok.Data;

@Data
public class UserTemplateNoticeVo {
    private Integer templateNo;     // TEMPLATE_NO
    private Integer userNo;         // USER_NO
    private String userId;          // USER_ID
    private String templateNm;      // TEMPLATE_NM
    private String delYn;           // DEL_YN
    private Integer repeatType;     // REPEAT_TYPE
    private String alarmDt;       // ALARM_DT (예: "2025-08-05 10:00:00")
}