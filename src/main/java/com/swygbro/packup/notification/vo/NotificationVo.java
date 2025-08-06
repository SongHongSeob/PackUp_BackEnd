package com.swygbro.packup.notification.vo;

import lombok.Data;

/**
 * TBL_NOTIFICATION 테이블 insert용 VO
 */
@Data
public class NotificationVo {
    private String userId;                // USER_ID
    private String message;               // MESSAGE
    private String templateNm ;           // TEMPLATE_NM
    private Integer templateNo;           // TEMPLATE_NO
    private String notificationTime;      // NOTIFICATION_TIME (yyyy-MM-dd HH:mm)
    private String sentAt;                // SENT_AT (yyyy-MM-dd HH:mm)
    private boolean sent = true;          // SENT
    private boolean readYn = false;       // READ_YN
}