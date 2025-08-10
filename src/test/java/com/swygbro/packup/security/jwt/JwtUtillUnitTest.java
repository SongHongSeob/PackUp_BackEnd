package com.swygbro.packup.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JwtUtillUnitTest {
    
    private JwtUtill jwtUtill;
    
    @BeforeEach
    void setUp() {
        // 테스트용 JWT 유틸 생성 (의존성 없이)
        String testSecret = "thisIsASecretKeyWithAtLeast32ByteLength123!!";
        jwtUtill = new JwtUtill(testSecret);
    }
    
    @Test
    void createAndValidateToken() {
        // given
        String username = "testuser";
        String role = "USER";
        String userId = "abc124";
        long expiredMs = 1000 * 60 * 10; // 10분
        
        // when
        String token = jwtUtill.createToken(username, role, userId, expiredMs);
        
        // then
        assertEquals(username, jwtUtill.getUsername(token));
        assertEquals(role, jwtUtill.getrole(token));
        assertEquals(userId, jwtUtill.getUserId(token));
        assertFalse(jwtUtill.isExpired(token));
        assertTrue(jwtUtill.validateToken(token));
    }
    
    @Test
    void testTokenExpiration() throws InterruptedException {
        // given
        String token = jwtUtill.createToken("expiredUser", "USER", "user1", 1000L); // 1초
        
        // wait 2 seconds to ensure it's expired
        Thread.sleep(2000);
        
        // then
        assertTrue(jwtUtill.isExpired(token));
        assertFalse(jwtUtill.validateToken(token));
    }
    
    @Test
    void testAutoLoginToken() {
        // given
        String username = "autoLoginUser";
        String role = "USER";
        String userId = "auto123";
        
        // when
        String token = jwtUtill.createAutoLoginToken(username, role, userId);
        
        // then
        assertEquals(username, jwtUtill.getUsername(token));
        assertEquals(role, jwtUtill.getrole(token));
        assertEquals(userId, jwtUtill.getUserId(token));
        assertTrue(jwtUtill.isLongTermToken(token));
        
        JwtUtill.AutoLoginResult result = jwtUtill.checkAutoLogin(token);
        assertTrue(result.isSuccess());
        assertEquals(username, result.getUsername());
        assertEquals(role, result.getRole());
        assertEquals(userId, result.getUserId());
    }
    
    @Test
    void testInvalidToken() {
        // given
        String invalidToken = "invalid.token.here";
        
        // when & then
        assertFalse(jwtUtill.validateToken(invalidToken));
        assertNull(jwtUtill.getAllClaims(invalidToken));
        
        JwtUtill.AutoLoginResult result = jwtUtill.checkAutoLogin(invalidToken);
        assertFalse(result.isSuccess());
    }
}
