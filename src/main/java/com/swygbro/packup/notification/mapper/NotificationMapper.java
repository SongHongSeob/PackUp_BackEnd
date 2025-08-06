package com.swygbro.packup.notification.mapper;

import com.swygbro.packup.notification.vo.NotificationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface NotificationMapper {

    /**
     * 알림 내역 테이블에 알림 데이터 저장
     */
    void insertNotification(NotificationVo notification);

    int countUnread(@Param("userId") String userId);

    void markAllAsRead(@Param("userId") String userId);

    List<NotificationVo> selectRecentNotifications(@Param("userId") String userId, @Param("limit") int limit);

    void updateWebhookUrl(@Param("userId") String userId, @Param("webhookUrl") String webhookUrl);

    Optional<String> getWebhookUrl(String userId);
}
