package com.swygbro.packup.config;

import com.swygbro.packup.security.jwt.JwtAuthenticationFilter;
import com.swygbro.packup.security.jwt.JwtUtill;
import com.swygbro.packup.security.oauth2.CustomAuthenticationEntryPoint;
import com.swygbro.packup.security.oauth2.CustomOAuth2UserService;
import com.swygbro.packup.security.oauth2.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final OAuth2SuccessHandler OAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final JwtUtill jwtUtill;

    public SecurityConfig(CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
                          OAuth2SuccessHandler OAuth2SuccessHandler,
                          CustomOAuth2UserService customOAuth2UserService,
                          CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                          JwtUtill jwtUtill) {
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.OAuth2SuccessHandler = OAuth2SuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.jwtUtill = jwtUtill;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                
                // JWT 인증 필터 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtill), UsernamePasswordAuthenticationFilter.class)
                
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/webjars/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/lgn/**","/register/*","/register","/lgn").permitAll()     // lgn/** → lgn/* 변경
                        .requestMatchers("/test*").permitAll()
                        .requestMatchers("/sample/**").permitAll()
                        .requestMatchers("/component/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/temp/**").permitAll()
                        .requestMatchers("/files/**").permitAll()
                        .requestMatchers("/api/**").permitAll()  // 통합 인증 API 포함
                        .requestMatchers("/notifications/**").permitAll()
                        .requestMatchers("/dashboard/**").permitAll()
                        .requestMatchers("/").permitAll()  // 루트 경로 허용
                        .requestMatchers("/mypage/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().hasAnyRole("ADMIN", "USER")
                )
                
                .formLogin((auth) -> auth
                        .loginPage("/lgn/loginForm")                        // 폼 페이지만 별도 경로
                        .loginProcessingUrl("/loginProcess")        // API 경로와 분리
                        .usernameParameter("username")              // mngrId → username
                        .passwordParameter("password")              // pswd → password
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureUrl("/lgn/login?error=true")
                        .permitAll()
                )

                // 소셜 로그인 관련 설정 추가
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(OAuth2SuccessHandler)   // 리다이렉트 및 토큰 발급
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // provider 응답 처리
                        )
                )
                
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/lgn/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "authorization")  // JWT 쿠키도 삭제
                        .permitAll()
                )
                
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                
                .build();
    }

    // ✅ 정적 리소스 완전 제외 (핵심!)
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/WEB-INF/**");
    }
    
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000", 
            "http://localhost:5173", 
            "https://packup.swygbro.com",
            "https://packupapi.xyz"  // 백엔드 도메인 추가
        )); 
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true); // 쿠키 허용
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}