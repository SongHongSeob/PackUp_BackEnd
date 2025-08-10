package com.swygbro.packup;

import com.swygbro.packup.security.jwt.JwtUtill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtill 단위 테스트 - Spring Context 없이 실행
 */
public class JwtUtillTest {
    
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
        long expiredMs = 1000 * 60 * 10;

        // when
        String token = jwtUtill.createToken(username, role, userId, expiredMs);

        // then
        assertEquals(username, jwtUtill.getUsername(token));
        assertEquals(role, jwtUtill.getrole(token));
        assertEquals(userId, jwtUtill.getUserId(token));
        assertFalse(jwtUtill.isExpired(token));

    }

    @Test
    void testTokenExpiration() throws InterruptedException {
        // given
        String token = jwtUtill.createToken("expiredUser", "USER", "user1", 1000L); // 1초짜리

        // wait 2 seconds to ensure it's expired
        Thread.sleep(2000);

        // then
        assertTrue(jwtUtill.isExpired(token));
    }
}
