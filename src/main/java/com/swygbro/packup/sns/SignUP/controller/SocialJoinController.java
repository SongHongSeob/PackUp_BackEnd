package com.swygbro.packup.sns.SignUP.controller;

import com.swygbro.packup.sns.Helper.socialLoginType;
import com.swygbro.packup.sns.SignUP.Service.JoinService;
import com.swygbro.packup.sns.SignUP.dto.JoinDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class SocialJoinController {

    private final JoinService joinService;

    // 회원가입 처리
    @PostMapping("/join")
    public ResponseEntity<Void> joinSocial(@RequestBody JoinDto dto) {
        joinService.joinSocial(dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 소셜 계정 가입 여부 확인 처리 -> OAuth2SuccessHandler에서 처리함. 개별 처리 불필요.

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // JWT 쿠키 제거
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setMaxAge(0);
        jwtCookie.setPath("/");

        /// 배포 도메인 기준 - CORS 문제 없게
        jwtCookie.setDomain("packup.swygbro.com");
        // Https 사용시
        // jwtCookie.setSecure(true);
        // jwtCookie.setHttpOnly(true);
        response.addCookie(jwtCookie);

        // 세션 무효화 (세션도 같이 썼다면)
        request.getSession().invalidate();

        return ResponseEntity.ok().body("로그아웃 성공");
    }


}
