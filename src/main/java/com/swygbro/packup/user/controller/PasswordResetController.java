package com.swygbro.packup.user.controller;

import com.swygbro.packup.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserService userService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            System.out.println("userId PasswordResetController : "+userId);
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("아이디를 입력해주세요.");
            }
            
            userService.sendPasswordResetEmail(userId);
            return ResponseEntity.ok("비밀번호 재설정 링크를 이메일로 발송했습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
        	
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("토큰이 필요합니다.");
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("새 비밀번호를 입력해주세요.");
            }
            
            if (newPassword.length() < 8) {
                return ResponseEntity.badRequest().body("비밀번호는 8자 이상이어야 합니다.");
            }
            
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }
    
    @GetMapping("/verify-token")
    public ResponseEntity<?> verifyResetToken(@RequestParam("token") String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("유효하지 않은 토큰입니다.");
            }
            
            // 토큰 유효성 검증
            if (!userService.validateResetToken(token)) {
                return ResponseEntity.badRequest().body("토큰이 만료되었거나 유효하지 않습니다.");
            }
            
            return ResponseEntity.ok("토큰이 유효합니다.");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("토큰이 만료되었거나 유효하지 않습니다.");
        }
    }
}