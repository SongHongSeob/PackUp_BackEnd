package com.swygbro.packup.login.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.swygbro.packup.security.jwt.JwtUtill;
import com.swygbro.packup.user.service.UserService;
import com.swygbro.packup.user.vo.UserVo;

import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lgn")
public class LoginController {

    private final UserService userService;
    private final JwtUtill jwtUtill;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserVo loginUser, HttpServletResponse response) {
        try {        	
            // 사용자 인증 로직 (UserService에 메서드 추가 필요)
            UserVo authenticatedUser = userService.authenticateUser(loginUser.getUserId(), loginUser.getUserPw());
            
            if (authenticatedUser != null) {
                // JWT 토큰 생성 (90일)
                String token = jwtUtill.createToken(
                    authenticatedUser.getUserNm(),
                    authenticatedUser.getRole(),
                    authenticatedUser.getUserId(),
                    90L * 24 * 60 * 60 * 1000
                );

                // JWT 토큰을 response body에 포함
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("success", true);
                responseBody.put("token", token);
                responseBody.put("userId", authenticatedUser.getUserId());
                responseBody.put("username", authenticatedUser.getUserNm());
                responseBody.put("role", authenticatedUser.getRole());
                
                return ResponseEntity.ok(responseBody);
            } else {
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("success", false);
                responseBody.put("message", "Invalid credentials");
                
                return ResponseEntity.badRequest().body(responseBody);
            }
        } catch (Exception e) {
            log.error("Login error: ", e);
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", false);
            responseBody.put("message", "Login failed");
            
            return ResponseEntity.internalServerError().body(responseBody);
        }
    }

    @GetMapping("/register")
    public ModelAndView register() {
        return new ModelAndView("login/register");
    }

    @GetMapping("/successLogin")
    public ModelAndView successLogin() {
        return new ModelAndView("login/successLogin");
    }

    @GetMapping("/faliureLogin")
    public ModelAndView faliureLogin() {
        return new ModelAndView("login/loginError");
    }

    @PostMapping("/insertUser")
    public ModelAndView insertUser(UserVo userDto) {
        log.info("insertUser ::: {}",userDto);
        int res = userService.insertUser(userDto);
        log.info("res ::: {}",res);

        if(res==1){
            return new ModelAndView("redirect:/index");
        }else{
            return new ModelAndView("redirect:/error/registerError");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // JWT 쿠키 제거 (authorization)
            Cookie authCookie = new Cookie("authorization", null);
            authCookie.setMaxAge(0);
            authCookie.setPath("/");
            authCookie.setDomain("packup.swygbro.com");
            response.addCookie(authCookie);

            // JWT 쿠키 제거 (jwt)
            Cookie jwtCookie = new Cookie("jwt", null);
            jwtCookie.setMaxAge(0);
            jwtCookie.setPath("/");
            jwtCookie.setDomain("packup.swygbro.com");
            response.addCookie(jwtCookie);

            // 세션 무효화
            if (request.getSession(false) != null) {
                request.getSession().invalidate();
            }

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("message", "일반 사용자 로그아웃 성공");
            
            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            log.error("일반 사용자 로그아웃 오류: ", e);
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", false);
            responseBody.put("message", "일반 사용자 로그아웃 실패");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        }
    }

}