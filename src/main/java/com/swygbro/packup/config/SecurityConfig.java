package com.swygbro.packup.config;

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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final OAuth2SuccessHandler OAuth2SuccessHandler;
    private final @Lazy CustomOAuth2UserService customOAuth2UserService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;


    public SecurityConfig(CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
                          OAuth2SuccessHandler OAuth2SuccessHandler,
                          @Lazy CustomOAuth2UserService customOAuth2UserService,
                          CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.OAuth2SuccessHandler = OAuth2SuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/webjars/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/lgn/*","/register/*","/register","/lgn").permitAll()     // lgn* → login*
                        .requestMatchers("/test*").permitAll()
                        .requestMatchers("/sample/**").permitAll()
                        .requestMatchers("/component/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/temp/**").permitAll()
                        .requestMatchers("/files/**").permitAll()
                        .requestMatchers("/notifications/**").permitAll()
                        .requestMatchers("/dashboard/**").permitAll()
                        .requestMatchers("/").permitAll()  // 루트 경로 허용
                        .requestMatchers("/mypage/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().hasAnyRole("ADMIN", "USER")
                )
                
                .formLogin((auth) -> auth
                        .loginPage("/lgn/login")                        // lgn → login
                        .loginProcessingUrl("/loginProcess")        // lgn/lgn → loginProcess
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
                        .deleteCookies("JSESSIONID")
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

}