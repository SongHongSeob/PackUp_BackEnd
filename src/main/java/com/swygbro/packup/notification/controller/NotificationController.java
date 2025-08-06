package com.swygbro.packup.notification.controller;

import com.swygbro.packup.config.CustomUserDetails;
import com.swygbro.packup.notification.service.NotificationService;
import com.swygbro.packup.notification.service.SseEmitterService;
import com.swygbro.packup.notification.vo.NotificationVo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final SseEmitterService sseEmitterService;
    private final NotificationService notificationService;

    // Slack OAuth 환경변수 주입
    @Value("${slack.client-id}")
    private String clientId;

    @Value("${slack.client-secret}")
    private String clientSecret;

    /**
     * 클라이언트에서 SSE 연결 요청
     * ex) EventSource('/notifications/subscribe?userId=user01')
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam("userId") String userId) {
        log.info("SSE require subscribe : userId={}", userId);
        return sseEmitterService.subscribe(userId);
    }

    @GetMapping("/unread_count")
    public Map<String, Integer> countUnread(@RequestParam String userId) {
        int count = notificationService.countUnread(userId);
        return Map.of("count", count);
    }

    @PostMapping("/readAll")
    public ResponseEntity<Void> markAllAsRead(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<NotificationVo>> getNotificationList(
            @RequestParam("userId") String userId,
            @RequestParam(defaultValue = "10") int limit) {

        List<NotificationVo> notifications = notificationService.getRecentNotifications(userId, limit);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/slack/callback")
    public void slackCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        String redirectUri = "https://packupapi.xyz/notifications/slack/callback";

        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(
                "https://slack.com/api/oauth.v2.access", request, Map.class);


        Map<String, Object> body = responseEntity.getBody();
        if (body != null && Boolean.TRUE.equals(body.get("ok"))) {
            Map<String, Object> incomingWebhook = (Map<String, Object>) body.get("incoming_webhook");
            String webhookUrl = (String) incomingWebhook.get("url");

            // 현재 로그인한 유저 정보 가져오기
            String userId = ((CustomUserDetails) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal()).getUsername();

            // DB 업데이트
            notificationService.updateWebhookUrl(userId, webhookUrl);

            // Webhook 저장 성공 후 바로 알림 발송
            notificationService.sendSlackNotification(userId, "🎉 Slack 알림 채널 연동이 완료되었습니다!");
        }

        // 리다이렉트 없이 빈 응답 반환
        response.sendRedirect("https://packup.swygbro.com");
    }
}