package com.swygbro.packup.notification.service;

import com.swygbro.packup.notification.vo.NotificationVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseEmitterService {

    // 사용자별 SseEmitter를 저장하는 Map
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // SSE 구독 요청시 emitter 생성 및 저장
    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(1000 * 60L * 30); // 30분 타임아웃
        emitters.put(userId, emitter);

        // 연결 완료 알림
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE connect success"));
        } catch (IOException e) {
            log.error("SSE connect fail", e);
        }

        // 연결 종료, 오류 시 emitter 제거
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        log.info("SSE subscribe connect success: userId={}", userId);
        return emitter;
    }

    // 알림 보내기 (알림 메시지 전달)
    public void send(NotificationVo notification) {
        SseEmitter emitter = emitters.get(notification.getUserId());
        if (emitter != null) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("templateNo", notification.getTemplateNo());
                payload.put("templateNm", notification.getTemplateNm());
                payload.put("message", notification.getMessage());
                payload.put("sentAt", notification.getSentAt());
                payload.put("readYn", notification.isReadYn());

                emitter.send(SseEmitter.event()
                        .name("alarm")
                        .data(payload));
                log.info("SSE success send notification : userId={}, message={}", notification.getUserId(), payload.get("message"));
            } catch (IOException e) {
                emitters.remove(notification.getUserId());
                log.error("SSE fail send notification : userId={}, fail={}", notification.getUserId(), e.getMessage());
            }
        } else {
            log.info("There is no SSE connect : userId={}", notification.getUserId());
        }
    }
}