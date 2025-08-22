package com.swygbro.packup.sns.calendar.service;

import com.swygbro.packup.sns.calendar.config.GoogleCalendarConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private final GoogleCalendarConfig googleCalendarConfig;

    /**
     * OAuth 2.0 인증 URL 생성
     */
    public String getAuthorizationUrl(String userId) {
        return "https://accounts.google.com/o/oauth2/auth"
                + "?client_id=" + googleCalendarConfig.getClientId()
                + "&redirect_uri=" + googleCalendarConfig.getRedirectUri()
                + "&scope=https://www.googleapis.com/auth/calendar"
                + "&response_type=code"
                + "&access_type=offline"
                + "&approval_prompt=force"
                + "&state=" + userId;
    }

    /**
     * OAuth 2.0 인증 코드를 사용하여 토큰 획득
     * 다른 팀원이 DB 저장을 담당하므로 토큰만 반환
     */
    public Map<String, Object> exchangeCodeForTokens(String code) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            Map<String, String> params = new HashMap<>();
            params.put("client_id", googleCalendarConfig.getClientId());
            params.put("client_secret", googleCalendarConfig.getClientSecret());
            params.put("redirect_uri", googleCalendarConfig.getRedirectUri());
            params.put("grant_type", "authorization_code");
            params.put("code", code);
            
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (!postData.isEmpty()) postData.append('&');
                postData.append(param.getKey()).append('=').append(param.getValue());
            }
            
            HttpEntity<String> request = new HttpEntity<>(postData.toString(), headers);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                    "https://oauth2.googleapis.com/token", request, 
                    (Class<Map<String, Object>>) (Class<?>) Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
            throw new RuntimeException("Failed to exchange code for tokens");
            
        } catch (Exception e) {
            log.error("Failed to exchange code for tokens", e);
            throw new RuntimeException("Failed to exchange code for tokens", e);
        }
    }

    /**
     * 템플릿 정보를 구글 캘린더에 이벤트로 생성
     * REST API 방식으로 직접 호출
     */
    public String createTemplateEvent(String accessToken, String templateNm, String description, 
                                    LocalDateTime startDateTime, LocalDateTime endDateTime, String location,
                                    Integer reminderMinutes) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            
            // RFC3339 형식으로 시간 포맷
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            String startTimeStr = startDateTime.format(formatter);
            String endTimeStr = endDateTime.format(formatter);
            
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("summary", "[PackUp] " + templateNm);
            eventData.put("description", description != null ? description : "PackUp 여행 일정: " + templateNm);
            if (location != null && !location.trim().isEmpty()) {
                eventData.put("location", location);
            }
            
            // 시작 시간 설정
            Map<String, Object> start = new HashMap<>();
            start.put("dateTime", startTimeStr);
            start.put("timeZone", "Asia/Seoul");
            eventData.put("start", start);
            
            // 종료 시간 설정
            Map<String, Object> end = new HashMap<>();
            end.put("dateTime", endTimeStr);
            end.put("timeZone", "Asia/Seoul");
            eventData.put("end", end);
            
            // 알림 설정 (사용자가 설정한 시간으로)
            if (reminderMinutes != null && reminderMinutes > 0) {
                Map<String, Object> reminder = new HashMap<>();
                reminder.put("method", "popup");
                reminder.put("minutes", reminderMinutes);
                
                Map<String, Object> reminders = new HashMap<>();
                reminders.put("useDefault", false);
                reminders.put("overrides", List.of(reminder));
                eventData.put("reminders", reminders);
            }
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(eventData, headers);
            
            // Google Calendar API 호출
            String url = "https://www.googleapis.com/calendar/v3/calendars/primary/events";
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, request, 
                    (Class<Map<String, Object>>) (Class<?>) Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String eventId = (String) responseBody.get("id");
                
                log.info("Calendar event created: {} for template: {} with reminder: {} minutes", 
                        eventId, templateNm, reminderMinutes);
                return eventId;
            }
            
            throw new RuntimeException("Failed to create calendar event");
            
        } catch (Exception e) {
            log.error("Failed to create calendar event for template: {}", templateNm, e);
            throw new RuntimeException("Failed to create calendar event", e);
        }
    }

    /**
     * 구글 캘린더 이벤트 삭제
     */
    public void deleteCalendarEvent(String accessToken, String eventId) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            // Google Calendar API 호출 - 이벤트 삭제
            String url = "https://www.googleapis.com/calendar/v3/calendars/primary/events/" + eventId;
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            
            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                log.info("Calendar event deleted: {}", eventId);
            } else {
                throw new RuntimeException("Failed to delete calendar event");
            }
            
        } catch (Exception e) {
            log.error("Failed to delete calendar event: {}", eventId, e);
            throw new RuntimeException("Failed to delete calendar event", e);
        }
    }

    /**
     * 액세스 토큰이 유효한지 확인
     * REST API 방식으로 간단한 호출
     */
    public boolean isTokenValid(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            // 간단한 캘린더 목록 조회로 토큰 유효성 확인
            String url = "https://www.googleapis.com/calendar/v3/users/me/calendarList?maxResults=1";
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, request, 
                    (Class<Map<String, Object>>) (Class<?>) Map.class);
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            log.warn("Invalid access token", e);
            return false;
        }
    }
}
