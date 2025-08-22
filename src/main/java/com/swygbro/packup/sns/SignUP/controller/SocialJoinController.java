package com.swygbro.packup.sns.SignUP.controller;

import com.swygbro.packup.sns.Helper.socialLoginType;
import com.swygbro.packup.sns.SignUP.Service.JoinService;
import com.swygbro.packup.sns.SignUP.dto.JoinDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class SocialJoinController {

    private final JoinService joinService;
    
    @Value("${app.cookie-domain:packup.swygbro.com}")
    private String cookieDomain;

    // 회원가입 처리
    @PostMapping("/join")
    public ResponseEntity<Void> joinSocial(@RequestBody JoinDto dto) {
        joinService.joinSocial(dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 소셜 계정 가입 여부 확인 처리 -> OAuth2SuccessHandler에서 처리함. 개별 처리 불필요.

    // SNS 로그아웃 (기존 방식 유지)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // JWT 쿠키 제거
            Cookie jwtCookie = new Cookie("jwt", null);
            jwtCookie.setMaxAge(0);
            jwtCookie.setPath("/");
            jwtCookie.setDomain(cookieDomain);
            response.addCookie(jwtCookie);

            // Authorization 쿠키도 제거
            Cookie authCookie = new Cookie("Authorization", null);
            authCookie.setMaxAge(0);
            authCookie.setPath("/");
            authCookie.setDomain(cookieDomain);
            response.addCookie(authCookie);

            // 세션 무효화 (세션도 같이 썼다면)
            if (request.getSession(false) != null) {
                request.getSession().invalidate();
            }

            return ResponseEntity.ok().body("SNS 로그아웃 성공");
            
        } catch (Exception e) {
            log.error("SNS 로그아웃 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("SNS 로그아웃 실패");
        }
    }


}
