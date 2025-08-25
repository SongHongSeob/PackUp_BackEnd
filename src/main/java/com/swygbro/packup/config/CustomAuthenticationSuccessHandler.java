package com.swygbro.packup.config;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.swygbro.packup.security.jwt.JwtUtill;
import com.swygbro.packup.user.Mapper.UserMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserMapper userMapper;
    private final JwtUtill jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        String userId = authentication.getName();
        String username = userId; // 일반 로그인에서는 username과 userId가 동일

        System.out.println("userId : "+userId);
        
        try {
            userMapper.updateLastLoginDate(userId);
            log.info("Updated last login date for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to update last login date for user: {}", userId, e);
        }
        
        // 사용자 권한 가져오기
        String role = "ROLE_USER"; // 기본값
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            GrantedAuthority authority = authentication.getAuthorities().iterator().next();
            role = authority.getAuthority();
        }
        
        // JWT 토큰 생성 (7일 유효)
        String token = jwtUtil.createToken(username, role, userId, 7L * 24 * 60 * 60 * 1000);
        
        // 쿠키에 토큰 저장
        ResponseCookie responseCookie = createCookie("Authorization", token);
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
        
        log.info("JWT token created and set in cookie for user: {}", userId);
        
        response.sendRedirect("/dashboard/dashboard");
    }
    
    /**
     * JWT 토큰을 저장할 쿠키 생성
     */
    private ResponseCookie createCookie(String key, String value) {
        return ResponseCookie.from(key, value)
                .httpOnly(true)     // XSS 방지를 위해 httpOnly true
                .secure(false)      // 개발환경에서는 false, 운영환경에서는 true
                .path("/")
                .maxAge(30 * 24 * 60 * 60) // 30일
                .sameSite("Lax")    // CSRF 방지
                .build();
    }
}