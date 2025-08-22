package com.swygbro.packup;

import com.swygbro.packup.security.jwt.JwtUtill;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CI 환경에서 안전하게 실행될 수 있는 간단한 애플리케이션 테스트
 */
class SimpleApplicationTest {

    @Test
    void jwtUtilCanBeInstantiated() {
        // JwtUtill이 정상적으로 인스턴스화되는지 확인
        String testSecret = "thisIsASecretKeyWithAtLeast32ByteLength123!!";
        
        assertDoesNotThrow(() -> {
            JwtUtill jwtUtill = new JwtUtill(testSecret);
            assertNotNull(jwtUtill);
        });
    }

    @Test
    void applicationClassExists() {
        // 메인 애플리케이션 클래스가 존재하는지 확인
        assertDoesNotThrow(() -> {
            Class<?> clazz = PackUpApplication.class;
            assertNotNull(clazz);
            assertEquals("PackUpApplication", clazz.getSimpleName());
        });
    }
}
