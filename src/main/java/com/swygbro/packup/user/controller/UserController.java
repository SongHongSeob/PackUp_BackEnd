package com.swygbro.packup.user.controller;

import com.swygbro.packup.user.service.UserService;
import com.swygbro.packup.user.vo.UserVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * JWT 토큰으로 현재 로그인한 사용자 정보 조회
     */
    @PostMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        System.out.println("authentication : "+authentication);
        try {
            if (authentication == null || authentication.getName() == null) {
                response.put("success", false);
                response.put("message", "인증되지 않은 사용자입니다.");
                return ResponseEntity.status(401).body(response);
            }

            String userId = authentication.getName();
            log.info("Getting user info for: {}", userId);
            
            UserVo userInfo = userService.getUserInfo(userId);
            
            if (userInfo != null) {
                // 비밀번호 제거
                userInfo.setUserPw(null);
                
                response.put("success", true);
                response.put("user", userInfo);
                response.put("userId", userInfo.getUserId());
                response.put("username", userInfo.getUserNm());
                response.put("role", userInfo.getRole());
            } else {
                response.put("success", false);
                response.put("message", "사용자 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(response);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting current user info: ", e);
            response.put("success", false);
            response.put("message", "사용자 정보 조회 중 오류가 발생했습니다.");
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * JWT 토큰 유효성 및 사용자 정보 간단 조회
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAuth(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null || authentication.getName() == null) {
            response.put("authenticated", false);
            response.put("message", "인증되지 않은 사용자입니다.");
            return ResponseEntity.status(401).body(response);
        }

        response.put("authenticated", true);
        response.put("userId", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        
        return ResponseEntity.ok(response);
    }
}