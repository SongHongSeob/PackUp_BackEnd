package com.swygbro.packup.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtill {

    private SecretKey key;

    public JwtUtill(@Value("${spring.jwt.secret}") String secret) {
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // jwt bearner 토큰으로 부터 username을 추출
    public String getUsername(String jws) {
        // 만약 "isAdmin"; true 필드를 원할 경우 String.class -> Boolean.class로 변경하기.
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(jws).getPayload().get("username", String.class);
    }

    // jwt bearer 토큰으로부터 role을 추출
    public String getrole(String jws) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(jws).getPayload().get("role", String.class);
    }

    // jwt bearer 토큰으로부터 userId을 추출
    public  String getUserId(String jws) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(jws).getPayload().get("userId", String.class);
    }

    // jwt bearer 토큰이 만료 되었는지 확인
    public Boolean isExpired(String jws) {
        try{
            Date expire = Jwts.parser().verifyWith(key).build().parseSignedClaims(jws).getPayload().getExpiration();

            System.out.println("만료 시간: " + expire);
            return expire.before(new Date());

        }catch (ExpiredJwtException e){
            System.out.println("JWT가 이미 만료됨: " + e.getMessage());
            return true;
        }
    }

    /**
     * jwt 토큰 생성
     * @param username, role: payload에 넣을 사용자 정보
     * @param expiredMs: 만료 시간(밀리초 단위)
     * @return
     */
    public String createToken(String username, String role, String userId, Long expiredMs) {
        return Jwts.builder()
                .claim("username", username)
                .claim("role", role)
                .claim("userId", userId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(key)
                .compact();
    }

    /**
     * JWT 토큰 전체 검증 (존재, 만료, 서명 검증)
     * @param token JWT 토큰
     * @return 검증 결과 (true: 유효, false: 무효)
     */
    public boolean validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                System.out.println("토큰이 null 또는 빈 문자열입니다.");
                return false;
            }

            // 토큰 파싱 (서명 검증 포함)
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            
            // 만료 시간 확인
            if (isExpired(token)) {
                System.out.println("토큰이 만료되었습니다.");
                return false;
            }
            
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("토큰이 만료됨: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("토큰 검증 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 토큰에서 모든 클레임 정보 추출
     * @param token JWT 토큰
     * @return Claims 객체 (토큰이 유효하지 않으면 null)
     */
    public Claims getAllClaims(String token) {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            System.out.println("클레임 추출 실패: " + e.getMessage());
            return null;
        }
    }

    /**
     * 토큰 검증 상세 정보 반환
     * @param token JWT 토큰
     * @return 검증 결과 및 상세 정보
     */
    public TokenValidationResult validateTokenWithDetails(String token) {
        TokenValidationResult result = new TokenValidationResult();
        
        try {
            if (token == null || token.trim().isEmpty()) {
                result.setValid(false);
                result.setMessage("토큰이 null 또는 빈 문자열입니다.");
                return result;
            }

            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            
            result.setValid(true);
            result.setMessage("토큰이 유효합니다.");
            result.setUsername((String) claims.get("username"));
            result.setRole((String) claims.get("role"));
            result.setUserId((String) claims.get("userId"));
            result.setIssuedAt(claims.getIssuedAt());
            result.setExpiresAt(claims.getExpiration());
            
            // 만료 확인
            if (isExpired(token)) {
                result.setValid(false);
                result.setMessage("토큰이 만료되었습니다.");
            }
            
        } catch (ExpiredJwtException e) {
            result.setValid(false);
            result.setMessage("토큰이 만료됨: " + e.getMessage());
        } catch (Exception e) {
            result.setValid(false);
            result.setMessage("토큰 검증 실패: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출 (쿠키 또는 Authorization 헤더에서)
     * @param request HTTP 요청
     * @return JWT 토큰 (토큰이 없으면 null)
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        // 1. Authorization 헤더에서 Bearer 토큰 찾기
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // 2. 쿠키에서 토큰 찾기
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Authorization".equals(cookie.getName()) || "jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }

    /**
     * JWT 토큰에서 사용자 ID 추출 (getUserId와 동일)
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public String getUserIdFromToken(String token) {
        return getUserId(token);
    }

    /**
     * JWT 토큰에서 사용자명 추출 (getUsername과 동일)
     * @param token JWT 토큰
     * @return 사용자명
     */
    public String getUsernameFromToken(String token) {
        return getUsername(token);
    }
     /* @param token JWT 토큰
     * @return 자동로그인 결과
     */
    public AutoLoginResult checkAutoLogin(String token) {
        AutoLoginResult result = new AutoLoginResult();
        
        try {
            if (token == null || token.trim().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("토큰이 존재하지 않습니다.");
                return result;
            }

            // 토큰 검증
            if (!validateToken(token)) {
                result.setSuccess(false);
                result.setMessage("유효하지 않은 토큰입니다.");
                return result;
            }

            // 사용자 정보 추출
            String username = getUsername(token);
            String role = getrole(token);
            String userId = getUserId(token);
            Claims claims = getAllClaims(token);

            result.setSuccess(true);
            result.setMessage("자동로그인 성공");
            result.setUsername(username);
            result.setRole(role);
            result.setUserId(userId);
            result.setIssuedAt(claims.getIssuedAt());
            result.setExpiresAt(claims.getExpiration());
            
            System.out.println("자동로그인 성공 - 사용자: " + username + ", 역할: " + role);
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("자동로그인 실패: " + e.getMessage());
            System.out.println("자동로그인 실패: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 자동로그인용 장기 토큰 생성 (기본 30일)
     * @param username 사용자명
     * @param role 역할
     * @param userId 사용자ID
     * @return JWT 토큰
     */
    public String createAutoLoginToken(String username, String role, String userId) {
        // 30일 = 30 * 24 * 60 * 60 * 1000 밀리초
        Long expiredMs = 30L * 24 * 60 * 60 * 1000;
        return createToken(username, role, userId, expiredMs);
    }

    /**
     * 자동로그인용 장기 토큰 생성 (사용자 정의 기간)
     * @param username 사용자명
     * @param role 역할
     * @param userId 사용자ID
     * @param days 토큰 유효 기간 (일)
     * @return JWT 토큰
     */
    public String createAutoLoginToken(String username, String role, String userId, int days) {
        Long expiredMs = (long) days * 24 * 60 * 60 * 1000;
        return createToken(username, role, userId, expiredMs);
    }

    /**
     * 토큰이 자동로그인용 장기 토큰인지 확인
     * @param token JWT 토큰
     * @return true: 장기 토큰 (7일 이상), false: 단기 토큰
     */
    public boolean isLongTermToken(String token) {
        try {
            Claims claims = getAllClaims(token);
            if (claims == null) return false;
            
            Date issuedAt = claims.getIssuedAt();
            Date expiresAt = claims.getExpiration();
            
            // 토큰 유효기간 계산 (밀리초)
            long validityPeriod = expiresAt.getTime() - issuedAt.getTime();
            // 7일을 밀리초로 변환
            long sevenDaysInMs = 7L * 24 * 60 * 60 * 1000;
            
            return validityPeriod >= sevenDaysInMs;
            
        } catch (Exception e) {
            System.out.println("장기 토큰 확인 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 자동로그인 결과를 담는 클래스
     */
    public static class AutoLoginResult {
        private boolean success;
        private String message;
        private String username;
        private String role;
        private String userId;
        private Date issuedAt;
        private Date expiresAt;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public Date getIssuedAt() { return issuedAt; }
        public void setIssuedAt(Date issuedAt) { this.issuedAt = issuedAt; }
        
        public Date getExpiresAt() { return expiresAt; }
        public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }
    }

    /**
     * 토큰 검증 결과를 담는 내부 클래스
     */
    public static class TokenValidationResult {
        private boolean valid;
        private String message;
        private String username;
        private String role;
        private String userId;
        private Date issuedAt;
        private Date expiresAt;

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public Date getIssuedAt() { return issuedAt; }
        public void setIssuedAt(Date issuedAt) { this.issuedAt = issuedAt; }
        
        public Date getExpiresAt() { return expiresAt; }
        public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }
    }

}

