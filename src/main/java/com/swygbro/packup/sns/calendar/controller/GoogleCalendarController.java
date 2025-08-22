package com.swygbro.packup.sns.calendar.controller;

import com.swygbro.packup.config.CustomUserDetails;
import com.swygbro.packup.sns.calendar.service.GoogleCalendarService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/calendar")
public class GoogleCalendarController {

    private final GoogleCalendarService googleCalendarService;

    /**
     * 구글 캘린더 OAuth 인증 시작
     */
    @GetMapping("/auth")
    public ResponseEntity<Map<String, String>> startAuth() {
        try {
            String userId = getCurrentUserId();
            String authUrl = googleCalendarService.getAuthorizationUrl(userId);
            
            return ResponseEntity.ok(Map.of(
                "authUrl", authUrl,
                "message", "구글 캘린더 인증을 위해 해당 URL로 이동하세요."
            ));
            
        } catch (Exception e) {
            log.error("Failed to start Google Calendar auth", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "인증 URL 생성에 실패했습니다."));
        }
    }

    /**
     * 구글 캘린더 OAuth 콜백 처리
     * 토큰 정보를 반환하여 다른 팀원이 DB에 저장하도록 함
     */
    @GetMapping("/callback")
    public void handleCallback(@RequestParam String code, 
                              @RequestParam(required = false) String state,
                              HttpServletResponse response) throws IOException {
        try {
            String userId = state != null ? state : getCurrentUserId();
            Map<String, Object> tokenInfo = googleCalendarService.exchangeCodeForTokens(code);
            
            // 테스트용 토큰 로그 출력
            log.info("=== 테스트용 토큰 정보 ===");
            log.info("Access Token: {}", tokenInfo.get("access_token"));
            log.info("Refresh Token: {}", tokenInfo.get("refresh_token"));
            log.info("========================");
            
            // 여기서 다른 팀원의 서비스를 호출하여 토큰 정보를 DB에 저장
            // 예: tokenStorageService.saveTokens(userId, tokenInfo);
            
            log.info("Google Calendar tokens obtained for user: {}", userId);
            
            // 성공 시 프론트엔드로 리다이렉트
            response.sendRedirect("https://packup.swygbro.com/calendar/success");
            
        } catch (Exception e) {
            log.error("Failed to handle Google Calendar callback", e);
            response.sendRedirect("https://packup.swygbro.com/calendar/error");
        }
    }

    /**
     * 템플릿 알림을 구글 캘린더에 생성
     * 프론트엔드에서 템플릿 알림 설정 시 호출
     */
    @PostMapping("/create-template-event")
    public ResponseEntity<Map<String, Object>> createTemplateEvent(
            @RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            String accessToken = (String) request.get("accessToken"); // 다른 팀원이 DB에서 조회해서 전달
            String templateNm = (String) request.get("templateNm");
            String description = (String) request.get("description");
            String location = (String) request.get("location");
            
            // 시간 파싱
            LocalDateTime startDate = LocalDateTime.parse((String) request.get("startDate"));
            LocalDateTime endDate = LocalDateTime.parse((String) request.get("endDate"));
            
            // 알림 시간 설정 (기본값: 30분)
            Integer reminderMinutes = 30; // 기본값
            if (request.get("reminderMinutes") != null) {
                reminderMinutes = (Integer) request.get("reminderMinutes");
            }
            
            String eventId = googleCalendarService.createTemplateEvent(
                    accessToken, templateNm, description, startDate, endDate, location, reminderMinutes);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "eventId", eventId,
                "message", "템플릿 알림이 구글 캘린더에 생성되었습니다. (" + reminderMinutes + "분 전 알림)"
            ));
            
        } catch (Exception e) {
            log.error("Failed to create template event", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "캘린더 이벤트 생성에 실패했습니다."));
        }
    }

    /**
     * 액세스 토큰 유효성 확인
     * 템플릿 알림 설정 시 구글 연동 상태 확인용
     */
    @PostMapping("/check-token")
    public ResponseEntity<Map<String, Object>> checkToken(
            @RequestBody Map<String, String> request) {
        try {
            String accessToken = request.get("accessToken");
            boolean isValid = googleCalendarService.isTokenValid(accessToken);
            
            return ResponseEntity.ok(Map.of(
                "valid", isValid,
                "message", isValid ? "토큰이 유효합니다." : "토큰이 유효하지 않습니다."
            ));
            
        } catch (Exception e) {
            log.error("Failed to check token validity", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "토큰 확인에 실패했습니다."));
        }
    }

    /**
     * 피그마에서 보이는 "구글 연동 상태 확인" API
     * 프론트엔드에서 알림 설정 시 구글 연동 여부 확인
     */
    @GetMapping("/connection-status")
    public ResponseEntity<Map<String, Object>> checkConnectionStatus() {
        try {
            String userId = getCurrentUserId();
            
            // 여기서 다른 팀원의 서비스를 호출하여 토큰 존재 여부 확인
            // boolean hasToken = tokenStorageService.hasValidToken(userId);
            boolean hasToken = false; // 임시로 false
            
            return ResponseEntity.ok(Map.of(
                "connected", hasToken,
                "message", hasToken ? "구글 캘린더가 연동되어 있습니다." : "구글 캘린더 연동이 필요합니다."
            ));
            
        } catch (Exception e) {
            log.error("Failed to check connection status", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "연동 상태 확인에 실패했습니다."));
        }
    }

    /**
     * 구글 캘린더 이벤트 삭제
     * 템플릿 삭제 시 또는 알림 해제 시 사용
     */
    @DeleteMapping("/event/{eventId}")
    public ResponseEntity<Map<String, Object>> deleteCalendarEvent(
            @PathVariable String eventId,
            @RequestBody Map<String, String> request) {
        try {
            String accessToken = request.get("accessToken");
            googleCalendarService.deleteCalendarEvent(accessToken, eventId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "캘린더 이벤트가 삭제되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Failed to delete calendar event: {}", eventId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "이벤트 삭제에 실패했습니다."));
        }
    }

    /**
     * 테스트용 토큰 교환 API (JSON 응답)
     */
    @GetMapping("/test/token-exchange")
    public ResponseEntity<Map<String, Object>> testTokenExchange(@RequestParam String code) {
        try {
            Map<String, Object> tokenInfo = googleCalendarService.exchangeCodeForTokens(code);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "tokens", tokenInfo,
                "message", "토큰 교환 성공! access_token을 복사해서 사용하세요."
            ));
            
        } catch (Exception e) {
            log.error("Failed to exchange tokens", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "토큰 교환에 실패했습니다."));
        }
    }

    /**
     * 구글 캘린더 알림 시간 옵션 제공
     * 프론트엔드에서 드롭다운 선택지로 사용
     */
    @GetMapping("/reminder-options")
    public ResponseEntity<Map<String, Object>> getReminderOptions() {
        try {
            Map<String, Integer> reminderOptions = Map.of(
                "5분 전", 5,
                "10분 전", 10,
                "15분 전", 15,
                "30분 전", 30,
                "1시간 전", 60,
                "2시간 전", 120,
                "1일 전", 1440
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "options", reminderOptions,
                "defaultMinutes", 30
            ));
            
        } catch (Exception e) {
            log.error("Failed to get reminder options", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "알림 옵션 조회에 실패했습니다."));
        }
    }

    /**
     * 현재 로그인한 사용자 ID 조회
     */
    private String getCurrentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUsername();
    }
}
