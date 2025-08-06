package com.swygbro.packup.notification.service;

import com.swygbro.packup.notification.mapper.NotificationMapper;
import com.swygbro.packup.notification.mapper.UserTemplateNoticeMapper;
import com.swygbro.packup.notification.vo.NotificationVo;
import com.swygbro.packup.notification.vo.UserTemplateNoticeVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserTemplateNoticeMapper stepMapper;
    private final NotificationMapper notificationMapper;
    private final SseEmitterService sseEmitterService;

    public void checkAndSendNotifications() {
        String alarmDtTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        Map<String, String> param = new HashMap<>();
        param.put("alarmDtTime", alarmDtTime);

        List<UserTemplateNoticeVo> targetList = stepMapper.selectNoticeTargetList(param);
        log.info("[Notification scheduler] {} count : {}", alarmDtTime, targetList.size());

        for (UserTemplateNoticeVo target : targetList) {
            String message = String.format("[%s] reserved notificaion.", alarmDtTime.substring(11));

            NotificationVo notification = new NotificationVo();
            notification.setUserId(target.getUserId());
            notification.setMessage(message);
            notification.setTemplateNm(target.getTemplateNm());
            notification.setTemplateNo(target.getTemplateNo());
            notification.setNotificationTime(alarmDtTime);
            notification.setSentAt(alarmDtTime);
            notification.setSent(true);
            notification.setReadYn(false);

            // 알림 테이블에 저장
            notificationMapper.insertNotification(notification);
            log.info("Notification save success : userId={}, templateNo={}", notification.getUserId(), notification.getTemplateNo());

            // SSE 알림 전송
            sseEmitterService.send(notification.getUserId(), notification.getMessage());

            sendSlackNotification(notification.getUserId(), notification.getTemplateNm() + "의 알림시각입니다.");
        }
    }

    public int countUnread(String userId) {
        return notificationMapper.countUnread(userId);
    }

    public void markAllAsRead(String userId) {
        notificationMapper.markAllAsRead(userId);
    }

    public List<NotificationVo> getRecentNotifications(String userId, int limit) {
        return notificationMapper.selectRecentNotifications(userId, limit);
    }

    public void updateWebhookUrl(String userId, String webhookUrl) {
        notificationMapper.updateWebhookUrl(userId, webhookUrl);
    }

    /**
     * 사용자 ID에 해당하는 Slack Webhook URL을 DB에서 조회하여 메시지 전송
     */
    public void sendSlackNotification(String userId, String message) {
        // 1. DB에서 Webhook URL 조회
        Optional<String> optionalWebhookUrl = notificationMapper.getWebhookUrl(userId);

        if (optionalWebhookUrl.isEmpty()) {
            System.err.println("[Slack] Webhook URL이 존재하지 않습니다. userId: " + userId);
            return;
        }

        String webhookUrl = optionalWebhookUrl.get();

        // 2. Slack 메시지 JSON 구성
        String payload = "{ \"text\": \"" + message + "\" }";

        // 3. HTTP 요청 구성
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        // 4. Slack으로 POST 전송
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("[Slack] 메시지 전송 성공");
            } else {
                System.err.println("[Slack] 전송 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("[Slack] 예외 발생: " + e.getMessage());
        }
    }
}