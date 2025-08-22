package com.swygbro.packup.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtill jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        log.debug("JWT Filter processing path: {}", path);

        // 인증이 불필요한 경로는 건너뛰기
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 헤더에서 토큰 추출 시도
        String token = extractTokenFromHeader(request);
        
        // 헤더에서 토큰이 없으면 쿠키에서 추출 시도 (하위 호환성)
        if (token == null) {
            token = extractTokenFromCookie(request);
        }
        
        log.info("JWT Filter - Path: {}, Token found: {}", path, token != null);
        
        if (token == null) {
            log.debug("No JWT token found in Authorization header or cookies for path: {}", path);
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                log.debug("Available cookies:");
                for (Cookie cookie : cookies) {
                    log.debug("Cookie: {} = {}", cookie.getName(), cookie.getValue());
                }
            }
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 토큰 만료 확인
            if (jwtUtil.isExpired(token)) {
                log.debug("JWT token is expired");
                handleExpiredToken(response);
                return;
            }

            // 토큰에서 사용자 정보 추출
            String username = jwtUtil.getUsername(token);
            String role = jwtUtil.getrole(token);
            String userId = jwtUtil.getUserId(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Spring Security Context에 인증 정보 설정 (userId를 principal로 사용)
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userId,  // username 대신 userId 사용
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT authentication successful for user: {}, role: {}, userId: {}", username, role, userId);
            }

        } catch (Exception e) {
            log.error("JWT token processing error: ", e);
            handleInvalidToken(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // "Bearer " 제거
        }
        return null;
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if ("authorization".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private boolean isPublicPath(String path) {
        // 인증이 불필요한 경로들
        return path.startsWith("/lgn/") ||
               path.startsWith("/register") ||
               path.startsWith("/auth/") ||
               path.startsWith("/test") ||
               path.startsWith("/sample/") ||
               path.startsWith("/component/") ||
               path.startsWith("/files/") ||
               path.startsWith("/notifications/") ||
               path.startsWith("/dashboard/") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.equals("/");
    }

    private void handleExpiredToken(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"TOKEN_EXPIRED\",\"message\":\"JWT 토큰이 만료되었습니다.\"}");
    }

    private void handleInvalidToken(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"INVALID_TOKEN\",\"message\":\"유효하지 않은 JWT 토큰입니다.\"}");
    }
}