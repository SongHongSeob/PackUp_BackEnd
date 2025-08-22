package com.swygbro.packup.auth.controller;

import com.swygbro.packup.security.jwt.JwtUtill;
import com.swygbro.packup.sns.SignUP.repository.SnsSignUpRepo;
import com.swygbro.packup.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/sns")
@RequiredArgsConstructor
@Tag(name = "SNS 인증 관리", description = "SNS 로그인 사용자를 위한 인증 관리 API")
public class AuthController {

    private final JwtUtill jwtUtil;
    private final UserRepository userRepository;
    private final SnsSignUpRepo snsSignUpRepo;
    
    @Value("${app.cookie-domain:packup.swygbro.com}")
    private String cookieDomain;

    /**
     * SNS 사용자 로그아웃
     */
    @Operation(summary = "SNS 사용자 로그아웃", 
               description = "SNS 로그인 사용자를 위한 로그아웃 기능 (JWT 토큰 및 쿠키 제거)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "500", description = "로그아웃 처리 중 오류 발생")
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> snsLogout(
            HttpServletRequest request, 
            HttpServletResponse response) {
        
        Map<String, Object> responseBody = new HashMap<>();
        
        try {
            // JWT 토큰에서 사용자 정보 추출 (로그 목적)
            String token = jwtUtil.extractTokenFromRequest(request);
            String loggedOutUserId = null;
            
            if (token != null && jwtUtil.validateToken(token)) {
                try {
                    loggedOutUserId = jwtUtil.getUserIdFromToken(token);
                    
                    // SNS 사용자인지 확인
                    if (loggedOutUserId != null) {
                        var user = userRepository.findByUserId(loggedOutUserId);
                        if (user.isPresent()) {
                            int userNo = user.get().getUserNo();
                            int snsCount = snsSignUpRepo.countByUserNo(userNo);
                            
                            if (snsCount == 0) {
                                // SNS 사용자가 아닌 경우 경고 로그
                                log.warn("일반 사용자가 SNS 로그아웃 API를 사용했습니다: userId={}", loggedOutUserId);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("토큰에서 사용자 정보 추출 실패: {}", e.getMessage());
                }
            }

            // 1. SNS 로그인 관련 쿠키 제거
            clearCookie(response, "Authorization");
            clearCookie(response, "authorization"); 
            clearCookie(response, "jwt");

            // 2. 세션 무효화
            if (request.getSession(false) != null) {
                request.getSession().invalidate();
            }

            // 3. 성공 응답
            responseBody.put("success", true);
            responseBody.put("message", "SNS 로그아웃이 성공적으로 완료되었습니다.");
            
            // 로그 출력
            if (loggedOutUserId != null) {
                log.info("SNS 사용자 로그아웃 완료: userId={}", loggedOutUserId);
            } else {
                log.info("알 수 없는 SNS 사용자 로그아웃 완료");
            }
            
            return ResponseEntity.ok(responseBody);
            
        } catch (Exception e) {
            log.error("SNS 로그아웃 처리 중 오류 발생: {}", e.getMessage(), e);
            responseBody.put("success", false);
            responseBody.put("message", "SNS 로그아웃 처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(responseBody);
        }
    }

    /**
     * 쿠키 제거 헬퍼 메서드
     */
    private void clearCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setDomain(cookieDomain);
        // HTTPS 환경에서는 secure 설정
        // cookie.setSecure(true);
        response.addCookie(cookie);
    }

    /**
     * SNS 사용자 로그인 상태 확인
     */
    @Operation(summary = "SNS 사용자 로그인 상태 확인", 
               description = "SNS 로그인 사용자의 현재 로그인 상태와 사용자 정보를 확인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "상태 확인 성공"),
        @ApiResponse(responseCode = "401", description = "로그인되지 않음")
    })
    @PostMapping("/status")
    public ResponseEntity<Map<String, Object>> checkSnsAuthStatus(HttpServletRequest request) {
        Map<String, Object> responseBody = new HashMap<>();
        
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            
            if (token == null || !jwtUtil.validateToken(token)) {
                responseBody.put("authenticated", false);
                responseBody.put("message", "SNS 로그인되지 않은 상태입니다.");
                return ResponseEntity.ok(responseBody);
            }
            
            String userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            
            // 사용자 정보 조회
            var userOpt = userRepository.findByUserId(userId);
            if (userOpt.isEmpty()) {
                responseBody.put("authenticated", false);
                responseBody.put("message", "사용자 정보를 찾을 수 없습니다.");
                return ResponseEntity.ok(responseBody);
            }
            
            var user = userOpt.get();
            int snsCount = snsSignUpRepo.countByUserNo(user.getUserNo());
            
            if (snsCount == 0) {
                responseBody.put("authenticated", false);
                responseBody.put("message", "SNS 사용자가 아닙니다.");
                return ResponseEntity.ok(responseBody);
            }
            
            // SNS 로그인 정보 조회
            var snsUserOpt = snsSignUpRepo.findByUserNoAndLoginType(user.getUserNo(), "KAKAO")
                    .or(() -> snsSignUpRepo.findByUserNoAndLoginType(user.getUserNo(), "NAVER"))
                    .or(() -> snsSignUpRepo.findByUserNoAndLoginType(user.getUserNo(), "GOOGLE"));
            
            String loginType = null;
            if (snsUserOpt.isPresent()) {
                loginType = snsUserOpt.get().getLoginType();
            }
            
            responseBody.put("authenticated", true);
            responseBody.put("userId", userId);
            responseBody.put("username", username);
            responseBody.put("email", user.getEmail());
            responseBody.put("phoneNum", user.getPhoneNum());
            responseBody.put("isSnsUser", true);
            responseBody.put("loginType", loginType);
            responseBody.put("role", user.getRole());
            
            // 추가 정보 입력 필요 여부
            boolean needsAdditionalInfo = user.getPhoneNum() == null || 
                                         user.getPhoneNum().trim().isEmpty() ||
                                         user.getUserPw() == null || 
                                         user.getUserPw().trim().isEmpty();
            responseBody.put("needsAdditionalInfo", needsAdditionalInfo);
            
            return ResponseEntity.ok(responseBody);
            
        } catch (Exception e) {
            log.error("SNS 로그인 상태 확인 중 오류: {}", e.getMessage(), e);
            responseBody.put("authenticated", false);
            responseBody.put("message", "상태 확인 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(responseBody);
        }
    }
}
