package com.swygbro.packup;

import com.swygbro.packup.sns.SignUP.Service.JoinService;
import com.swygbro.packup.sns.SignUP.dto.JoinDto;
import com.swygbro.packup.sns.SignUP.entity.SnsUser;
import com.swygbro.packup.sns.SignUP.repository.SnsSignUpRepo;
import com.swygbro.packup.security.oauth2.CustomOAuth2UserService;
import com.swygbro.packup.user.entity.User;
import com.swygbro.packup.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SNS 회원가입 서비스 단위 테스트 - Mock 기반
 */
@ExtendWith(MockitoExtension.class)
public class SnsSignUpTest {

    @InjectMocks
    private JoinService joinService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SnsSignUpRepo snsSignUpRepo;

    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    void 소셜회원가입_성공() {
        // given
        JoinDto joinDto = JoinDto.builder()
                .USER_ID("testUser")
                .USER_NM("홍길동")
                .EMAIL("test@example.com")
                .LOGIN_TYPE("google")
                .SOCIAL_ID("google-123")
                .build();

        User user = User.builder()
                .userId(joinDto.getUSER_ID())
                .userNm(joinDto.getUSER_NM())
                .email(joinDto.getEMAIL())
                .build();

        when(snsSignUpRepo.existsBySocialIdAndLoginType(joinDto.getSOCIAL_ID(), joinDto.getLOGIN_TYPE())).thenReturn(false);
        when(userRepository.existsByUserId(joinDto.getUSER_ID())).thenReturn(false);
        when(userRepository.findByEmail(joinDto.getEMAIL())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        // when
        assertDoesNotThrow(() -> joinService.joinSocial(joinDto));

        // then
        verify(userRepository, times(2)).save(any(User.class)); // 저장 2회 (초기 저장 + 닉네임 업데이트)
        verify(snsSignUpRepo, times(1)).save(any(SnsUser.class));
    }

    @Test
    void 소셜회원가입_실패_이미가입된소셜() {
        // given
        JoinDto joinDto = JoinDto.builder()
                .USER_ID("testUser")
                .USER_NM("홍길동")
                .EMAIL("test@example.com")
                .LOGIN_TYPE("google")
                .SOCIAL_ID("google-123")
                .build();

        when(snsSignUpRepo.existsBySocialIdAndLoginType(joinDto.getSOCIAL_ID(), joinDto.getLOGIN_TYPE())).thenReturn(true);

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            joinService.joinSocial(joinDto);
        });

        assertEquals("이미 가입된 SNS 계정입니다.", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(snsSignUpRepo, never()).save(any());
    }

    @Test
    void SNS사용자_연동_보장_성공() {
        // given
        String provider = "GOOGLE";
        String socialId = "google_12345";
        String email = "test@example.com";
        String candidateName = "테스트사용자";

        when(snsSignUpRepo.findBySocialIdAndLoginType(socialId, provider)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        
        User savedUser = User.builder()
                .userId(email)
                .userNm(candidateName)
                .email(email)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(snsSignUpRepo.existsByUserNoAndLoginType(anyInt(), eq(provider))).thenReturn(false);

        // when
        assertDoesNotThrow(() -> joinService.ensureSnsUserLinked(provider, socialId, email, candidateName));

        // then
        verify(userRepository, times(2)).save(any(User.class)); // 초기 저장 + 닉네임 업데이트
        verify(snsSignUpRepo, times(1)).save(any(SnsUser.class));
    }
}
