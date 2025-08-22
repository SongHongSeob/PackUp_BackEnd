package com.swygbro.packup.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 모든 경로에 대해
                .allowedOrigins("https://packup.swygbro.com", "http://localhost:5173", "http://localhost:3000")  // 프론트 도메인 허용
                .allowedMethods("*")  // GET, POST, PUT, DELETE 등
                .allowedHeaders("*")  // 모든 헤더 허용
                .allowCredentials(true); // 인증 정보(쿠키, 세션 등) 포함 허용 (필요 시)
    }
}