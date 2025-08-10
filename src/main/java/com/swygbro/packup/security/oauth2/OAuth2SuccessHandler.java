package com.swygbro.packup.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swygbro.packup.security.jwt.JwtUtill;
import com.swygbro.packup.sns.Helper.socialLoginType;
import com.swygbro.packup.sns.SignUP.Service.JoinService;
import com.swygbro.packup.sns.SignUP.dto.CustomOAuth2User;
import com.swygbro.packup.sns.SignUP.dto.SnsAuthResponseDto;
import com.swygbro.packup.sns.SignUP.entity.SnsUser;
import com.swygbro.packup.sns.SignUP.repository.SnsSignUpRepo;
import com.swygbro.packup.user.entity.User;
import com.swygbro.packup.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtill jwtUtil;
    private final SnsSignUpRepo snsSignUpRepo;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final JoinService joinService;

    public OAuth2SuccessHandler(JwtUtill jwtUtil, SnsSignUpRepo snsSignUpRepo, 
                               UserRepository userRepository, ObjectMapper objectMapper,
                               @Lazy JoinService joinService) {
        this.jwtUtil = jwtUtil;
        this.snsSignUpRepo = snsSignUpRepo;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.joinService = joinService;
    }
    
    @Value("${app.frontend-url:https://packup.swygbro.com}")
    private String frontendUrl;
    
    @Value("${app.cookie-domain:packup.swygbro.com}")
    private String cookieDomain;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        // ✅ 사용자 정보 가져오기
        CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();

        int userNo = user.getUserNo();
        String username = user.getUserNm();
        String userId = user.getUserId();
        String socialId = user.getSocialId();
        socialLoginType loginType = user.getSocialLoginType();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.iterator().next().getAuthority();

        // 신규 사용자면 가입/연동 먼저 보장 (idempotent)
        if (user.isNewUser()) {
            joinService.ensureSnsUserLinked(
                    loginType.name(),
                    socialId,
                    user.getEmail(),
                    username // null 가능; 서비스에서 닉네임/임시비번 생성
            );
        }

        // SNS 링크 기준으로 최종 User 재조회 (신규 생성되었을 수 있음)
        SnsUser link = snsSignUpRepo.findBySocialIdAndLoginType(socialId, loginType.name())
                .orElseThrow(() -> new IllegalStateException("SNS 연동 정보를 찾을 수 없습니다."));
        User userEntity = userRepository.findByUserNo(link.getUserNo())
                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));

        // 재조회한 엔티티 값으로 변수 재할당 (변수명 유지)
        userNo = userEntity.getUserNo();
        username = userEntity.getUserNm();
        userId = userEntity.getUserId();

        // ✅ 사용자 정보 조회
        //User userEntity = userRepository.findByUserNo(userNo).orElse(null);

        // ✅ 추가 정보 입력 필요 여부 확인 (전화번호와 비밀번호가 없으면 추가 정보 필요)
        boolean needsAdditionalInfo = userEntity == null || 
                                     userEntity.getPhoneNum() == null || 
                                     userEntity.getPhoneNum().trim().isEmpty() ||
                                     userEntity.getUserPw() == null || 
                                     userEntity.getUserPw().trim().isEmpty();

        // ✅ 응답 DTO 생성
        SnsAuthResponseDto responseDto = SnsAuthResponseDto.builder()
                .userId(userId)
                .userNm(username)
                .email(userEntity != null ? userEntity.getEmail() : null)
                .socialId(socialId)
                .loginType(loginType.name())
                .needsAdditionalInfo(needsAdditionalInfo)
                .build();

        // ✅ 토큰 발급 및 쿠키 저장
        String token = jwtUtil.createToken(username, role, userId, 90L * 24 * 60 * 60 * 1000);
        ResponseCookie responseCookie = createCookie("Authorization", token);
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        // ✅ SNS 연동 정보 저장 (중복 방지)
        if (!snsSignUpRepo.existsByUserNoAndLoginType(userNo, String.valueOf(loginType))) {
            SnsUser snsUser = SnsUser.builder()
                    .userNo(userNo)
                    .userId(userId)
                    .loginType(loginType.name())
                    .socialId(socialId)
                    .build();
            snsSignUpRepo.save(snsUser);
        }

        // ✅ 프론트에 사용자 정보를 쿼리 파라미터로 전달
        String userInfoJson = objectMapper.writeValueAsString(responseDto);
        String encodedUserInfo = URLEncoder.encode(userInfoJson, StandardCharsets.UTF_8);
        
        // 환경변수 기반 리다이렉트 URL 설정
        String redirectUrl = frontendUrl + "?userInfo=" + encodedUserInfo;
        response.sendRedirect(redirectUrl);
    }

    private ResponseCookie createCookie(String key, String value) {
        return ResponseCookie.from(key, value)
                .httpOnly(false)
                .secure(true)
                .path("/")
                .maxAge(90 * 24 * 60 * 60)
                .sameSite("None")
                .domain(cookieDomain)
                .build();
    }
}
