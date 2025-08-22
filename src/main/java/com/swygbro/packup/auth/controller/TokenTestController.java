package com.swygbro.packup.auth.controller;

import com.swygbro.packup.security.jwt.JwtUtill;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/token")
@RequiredArgsConstructor
public class TokenTestController {

    private final JwtUtill jwtUtil;

    /**
     * 테스트용 JWT 토큰 생성
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateTestToken(
            @RequestParam(defaultValue = "testUser") String username,
            @RequestParam(defaultValue = "USER") String role,
            @RequestParam(defaultValue = "123") String userId,
            @RequestParam(defaultValue = "3600000") Long expiredMs) {
        
        String token = jwtUtil.createToken(username, role, userId, expiredMs);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("username", username);
        response.put("role", role);
        response.put("userId", userId);
        response.put("expiresInMs", expiredMs);
        
        return ResponseEntity.ok(response);
    }

    /**
     * JWT 토큰 검증 (간단)
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        
        boolean isValid = jwtUtil.validateToken(token);
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        response.put("message", isValid ? "토큰이 유효합니다." : "토큰이 유효하지 않습니다.");
        
        return ResponseEntity.ok(response);
    }

    /**
     * JWT 토큰 검증 (상세 정보 포함)
     */
    @PostMapping("/validate-details")
    public ResponseEntity<JwtUtill.TokenValidationResult> validateTokenWithDetails(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        
        JwtUtill.TokenValidationResult result = jwtUtil.validateTokenWithDetails(token);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Authorization 헤더에서 토큰 검증
     */
    @GetMapping("/validate-header")
    public ResponseEntity<Map<String, Object>> validateTokenFromHeader(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.put("valid", false);
            response.put("message", "Authorization 헤더가 없거나 Bearer 형식이 아닙니다.");
            return ResponseEntity.badRequest().body(response);
        }
        
        String token = authorization.substring(7); // "Bearer " 제거
        JwtUtill.TokenValidationResult result = jwtUtil.validateTokenWithDetails(token);
        
        response.put("valid", result.isValid());
        response.put("message", result.getMessage());
        response.put("username", result.getUsername());
        response.put("role", result.getRole());
        response.put("userId", result.getUserId());
        response.put("issuedAt", result.getIssuedAt());
        response.put("expiresAt", result.getExpiresAt());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 쿠키에서 토큰 검증 (현재 JwtFilterForSns 방식과 동일)
     */
    @GetMapping("/validate-cookie")
    public ResponseEntity<Map<String, Object>> validateTokenFromCookie(
            @CookieValue(value = "authorization", required = false) String token) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (token == null) {
            response.put("valid", false);
            response.put("message", "authorization 쿠키가 없습니다.");
            return ResponseEntity.badRequest().body(response);
        }
        
        JwtUtill.TokenValidationResult result = jwtUtil.validateTokenWithDetails(token);
        
        response.put("valid", result.isValid());
        response.put("message", result.getMessage());
        response.put("username", result.getUsername());
        response.put("role", result.getRole());
        response.put("userId", result.getUserId());
        response.put("issuedAt", result.getIssuedAt());
        response.put("expiresAt", result.getExpiresAt());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 토큰에서 사용자 정보만 추출
     */
    @PostMapping("/extract-user")
    public ResponseEntity<Map<String, Object>> extractUserInfo(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = jwtUtil.getUsername(token);
            String role = jwtUtil.getrole(token);
            String userId = jwtUtil.getUserId(token);
            
            response.put("success", true);
            response.put("username", username);
            response.put("role", role);
            response.put("userId", userId);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "토큰에서 사용자 정보 추출 실패: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 자동로그인용 장기 토큰 생성 (30일)
     */
    @PostMapping("/generate-auto-login")
    public ResponseEntity<Map<String, Object>> generateAutoLoginToken(
            @RequestParam String username,
            @RequestParam(defaultValue = "USER") String role,
            @RequestParam String userId,
            @RequestParam(defaultValue = "30") int days) {
        
        String token = jwtUtil.createAutoLoginToken(username, role, userId, days);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("username", username);
        response.put("role", role);
        response.put("userId", userId);
        response.put("validDays", days);
        response.put("message", days + "일간 유효한 자동로그인 토큰이 생성되었습니다.");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 자동로그인 체크
     */
    @PostMapping("/check-auto-login")
    public ResponseEntity<JwtUtill.AutoLoginResult> checkAutoLogin(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        
        JwtUtill.AutoLoginResult result = jwtUtil.checkAutoLogin(token);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 쿠키에서 자동로그인 체크
     */
    @GetMapping("/auto-login-cookie")
    public ResponseEntity<Map<String, Object>> autoLoginFromCookie(
            @CookieValue(value = "authorization", required = false) String token) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (token == null) {
            response.put("success", false);
            response.put("message", "자동로그인 쿠키가 없습니다.");
            return ResponseEntity.ok(response);
        }
        
        JwtUtill.AutoLoginResult result = jwtUtil.checkAutoLogin(token);
        
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        response.put("username", result.getUsername());
        response.put("role", result.getRole());
        response.put("userId", result.getUserId());
        response.put("issuedAt", result.getIssuedAt());
        response.put("expiresAt", result.getExpiresAt());
        response.put("isLongTerm", jwtUtil.isLongTermToken(token));
        
        return ResponseEntity.ok(response);
    }

    /**
     * 토큰이 장기 토큰인지 확인
     */
    @PostMapping("/check-long-term")
    public ResponseEntity<Map<String, Object>> checkLongTermToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        
        Map<String, Object> response = new HashMap<>();
        
        boolean isLongTerm = jwtUtil.isLongTermToken(token);
        response.put("isLongTerm", isLongTerm);
        response.put("message", isLongTerm ? "장기 토큰입니다 (7일 이상)" : "단기 토큰입니다 (7일 미만)");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 쿠키에 저장된 JWT 토큰 정보 확인
     */
    @GetMapping("/cookie-info")
    public ResponseEntity<Map<String, Object>> getCookieInfo(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            response.put("hasCookie", false);
            response.put("message", "쿠키가 없습니다.");
            return ResponseEntity.ok(response);
        }
        
        Cookie authCookie = null;
        for (Cookie cookie : cookies) {
            if ("authorization".equals(cookie.getName())) {
                authCookie = cookie;
                break;
            }
        }
        
        if (authCookie == null) {
            response.put("hasCookie", false);
            response.put("message", "authorization 쿠키가 없습니다.");
            response.put("availableCookies", java.util.Arrays.stream(cookies)
                    .map(Cookie::getName).toArray());
        } else {
            String token = authCookie.getValue();
            JwtUtill.AutoLoginResult result = jwtUtil.checkAutoLogin(token);
            
            response.put("hasCookie", true);
            response.put("cookieName", authCookie.getName());
            response.put("tokenLength", token.length());
            response.put("tokenPreview", token.length() > 20 ? token.substring(0, 20) + "..." : token);
            response.put("tokenValid", result.isSuccess());
            response.put("username", result.getUsername());
            response.put("role", result.getRole());
            response.put("userId", result.getUserId());
            response.put("issuedAt", result.getIssuedAt());
            response.put("expiresAt", result.getExpiresAt());
            response.put("message", result.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}