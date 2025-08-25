package com.swygbro.packup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 애플리케이션 기본 테스트 - Spring Context 없이 실행
 */
public class ApplicationStartTest {

    @Test
    void applicationMainMethodExists() {
        // PackUpApplication 클래스가 존재하는지 확인
        assertDoesNotThrow(() -> {
            Class<?> clazz = Class.forName("com.swygbro.packup.PackUpApplication");
            assertNotNull(clazz);
            // main 메소드가 존재하는지 확인
            assertNotNull(clazz.getMethod("main", String[].class));
        });
    }

    @Test
    void basicApplicationPropertiesTest() {
        // 기본적인 시스템 속성 테스트
        String javaVersion = System.getProperty("java.version");
        assertNotNull(javaVersion);
        assertTrue(javaVersion.startsWith("17") || javaVersion.startsWith("1.8"));
    }
}
