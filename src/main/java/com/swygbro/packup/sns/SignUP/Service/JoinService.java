package com.swygbro.packup.sns.SignUP.Service;

import com.swygbro.packup.sns.SignUP.dto.JoinDto;
import com.swygbro.packup.sns.SignUP.entity.SnsUser;
import com.swygbro.packup.sns.SignUP.repository.SnsSignUpRepo;
import com.swygbro.packup.security.oauth2.CustomOAuth2UserService;
import com.swygbro.packup.user.entity.User;
import com.swygbro.packup.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JoinService {
    private final UserRepository userRepository;
    private final SnsSignUpRepo snsSignUpRepo;
    private final PasswordEncoder passwordEncoder;
    private final CustomOAuth2UserService customOAuth2UserService;
    
    public JoinService(UserRepository userRepository, SnsSignUpRepo snsSignUpRepo, 
                      PasswordEncoder passwordEncoder, @Lazy CustomOAuth2UserService customOAuth2UserService) {
        this.userRepository = userRepository;
        this.snsSignUpRepo = snsSignUpRepo;
        this.passwordEncoder = passwordEncoder;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    // OAuth2SuccessHandler에서 호출: 소셜 연동을 idempotent 하게 보장
    @Transactional
    public void ensureSnsUserLinked(String provider, String socialId, String email, String candidateName) {
        // 0) 이미 해당 socialId+provider 링크가 있으면 종료
        if (snsSignUpRepo.findBySocialIdAndLoginType(socialId, provider).isPresent()) {
            return;
        }

        // 1) email 기준 기존 사용자 찾기 (있으면 그 사용자에 링크)
        User user = null;
        if (email != null && !email.trim().isEmpty()) {
            var byEmail = userRepository.findByEmail(email.trim());
            if (byEmail.isPresent()) {
                user = byEmail.get();

                // 기존 사용자가 이미 다른 SNS에 연동돼 있다면 정책상 차단
                int snsCount = snsSignUpRepo.countByUserNo(user.getUserNo());
                if (snsCount > 0) {
                    throw new IllegalStateException("이미 다른 SNS 계정으로 가입된 이메일입니다. 기존 계정으로 로그인해주세요.");
                }
            }
        }

        // 2) 없으면 신규 User 생성
        if (user == null) {
            String userId = (email != null && !email.trim().isEmpty())
                    ? email.trim()
                    : (provider.toLowerCase() + "_" + extractIdSuffix(socialId));

            String encodedPw = passwordEncoder.encode(generateSecurePassword());

            user = User.builder()
                    .userId(userId)
                    .userNm((candidateName != null && !candidateName.isBlank()) ? candidateName : "packUp#temp")
                    .email((email != null && !email.isBlank()) ? email.trim() : null)
                    .userPw(encodedPw)
                    .role("ROLE_USER")
                    .useYn('Y')
                    .delYn('N')
                    .personalInfoAcq('Y')
                    .build();

            user = userRepository.save(user);

            // 닉네임을 packUp#회원번호로 업데이트
            user.setUserNm("packUp#" + user.getUserNo());
            userRepository.save(user);
        }

        // 3) SNS 링크 저장 (idempotent 체크)
        if (!snsSignUpRepo.existsByUserNoAndLoginType(user.getUserNo(), provider)) {
            SnsUser snsUser = SnsUser.builder()
                    .userNo(user.getUserNo())
                    .userId(user.getUserId())
                    .loginType(provider) // "KAKAO" / "NAVER" / "GOOGLE"
                    .socialId(socialId)  // "kakao_123..." 등
                    .regId("system")
                    .regDt(LocalDateTime.now())
                    .build();
            snsSignUpRepo.save(snsUser);
        }
    }

    // "kakao_12345" -> "12345" 같이 suffix만 추출
    private String extractIdSuffix(String socialId) {
        if (socialId == null) return "unknown";
        int idx = socialId.lastIndexOf('_');
        return (idx >= 0 && idx + 1 < socialId.length()) ? socialId.substring(idx + 1) : socialId;
    }

    @Transactional
    public void joinSocial(JoinDto joinDto) {
        log.info("SNS 회원가입 시작: userId={}, loginType={}, socialId={}", 
                joinDto.getUSER_ID(), joinDto.getLOGIN_TYPE(), joinDto.getSOCIAL_ID());

        // 1. SNS 계정 중복 확인
        if(snsSignUpRepo.existsBySocialIdAndLoginType(joinDto.getSOCIAL_ID(), joinDto.getLOGIN_TYPE())){
            log.warn("이미 가입된 SNS 계정: socialId={}, loginType={}", 
                    joinDto.getSOCIAL_ID(), joinDto.getLOGIN_TYPE());
            throw new IllegalStateException("이미 가입된 SNS 계정입니다.");
        }

        // 2. 이메일 기반 일반 회원 중복 체크 (이메일이 있는 경우에만)
        if (joinDto.getEMAIL() != null && !joinDto.getEMAIL().trim().isEmpty()) {
            var existingUserByEmail = userRepository.findByEmail(joinDto.getEMAIL());
            if (existingUserByEmail.isPresent()) {
                User existingUser = existingUserByEmail.get();
                int snsCount = snsSignUpRepo.countByUserNo(existingUser.getUserNo());
                
                if (snsCount == 0) {
                    // 일반 회원으로 이미 가입된 이메일
                    log.warn("이미 일반 회원으로 가입된 이메일: email={}", joinDto.getEMAIL());
                    throw new IllegalStateException("이미 일반 회원으로 가입된 이메일입니다. 핸드폰번호 인증을 통해 계정을 연동해주세요.");
                }
                // SNS 사용자인 경우는 CustomOAuth2UserService에서 처리됨
            }
        }

        // 3. 사용자 ID 중복 체크
        if (userRepository.existsByUserId(joinDto.getUSER_ID())) {
            log.warn("이미 존재하는 사용자 ID: userId={}", joinDto.getUSER_ID());
            throw new IllegalStateException("이미 존재하는 사용자 ID입니다.");
        }

        // 4. 임시 비밀번호 생성 및 암호화
        String rawPassword = generateSecurePassword();
        String encodedPw = passwordEncoder.encode(rawPassword);

        // 5. User 테이블 저장
        User user = User.builder()
                .userId(joinDto.getUSER_ID())
                .userNm(joinDto.getUSER_NM())
                .email(joinDto.getEMAIL())
                .userPw(encodedPw)  // 임시 비밀번호 설정
                .role("ROLE_USER")   // 기본 역할 설정
                .useYn('Y')         // 기본값 설정
                .delYn('N')         // 기본값 설정
                .personalInfoAcq('Y') // 기본값 설정
                .build();

        log.info("User 저장 시작: userId={}", user.getUserId());

        User savedUser = userRepository.save(user);
        
        log.info("User 저장 완료: userId={}, userNo={}", savedUser.getUserId(), savedUser.getUserNo());

        // 6. 닉네임을 packUp#회원번호로 업데이트
        savedUser.setUserNm("packUp#" + savedUser.getUserNo());
        userRepository.save(savedUser);

        // 7. SnsUser 저장
        SnsUser snsUser = SnsUser.builder()
                .userId(savedUser.getUserId())
                .userNo(savedUser.getUserNo())
                .loginType(joinDto.getLOGIN_TYPE())
                .socialId(joinDto.getSOCIAL_ID())
                .regId("system")
                .regDt(LocalDateTime.now())
                .build();

        log.info("SnsUser 저장: userId={}, userNo={}, loginType={}, socialId={}", 
                snsUser.getUserId(), snsUser.getUserNo(), snsUser.getLoginType(), snsUser.getSocialId());

        snsSignUpRepo.save(snsUser);
        
        log.info("SNS 회원가입 완료: userId={}, userNo={}", savedUser.getUserId(), savedUser.getUserNo());
    }

    /**
     * SNS 사용자 추가 정보 업데이트 (핸드폰번호, 비밀번호)
     */
    @Transactional
    public void updateSnsUserAdditionalInfo(String userId, String phoneNum, String password) {
        log.info("SNS 사용자 추가 정보 업데이트 시작: userId={}, phoneNum={}", userId, phoneNum);

        // 1. 사용자 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 2. SNS 사용자인지 확인
        int snsCount = snsSignUpRepo.countByUserNo(user.getUserNo());
        if (snsCount == 0) {
            throw new IllegalStateException("SNS 사용자가 아닙니다.");
        }

        // 3. 핸드폰번호 중복 체크 (일반 회원과의 중복 방지)
        if (phoneNum != null && !phoneNum.trim().isEmpty()) {
            // CustomOAuth2UserService의 검증 메서드 사용
            customOAuth2UserService.validatePhoneNumberForSnsUser(phoneNum, userId);
        }

        // 4. 비밀번호 암호화
        String encodedPassword = null;
        if (password != null && !password.trim().isEmpty()) {
            encodedPassword = passwordEncoder.encode(password);
        }

        // 5. 사용자 정보 업데이트
        if (phoneNum != null && !phoneNum.trim().isEmpty()) {
            user.setPhoneNum(phoneNum);
        }
        if (encodedPassword != null) {
            user.setUserPw(encodedPassword);
        }

        userRepository.save(user);
        
        log.info("SNS 사용자 추가 정보 업데이트 완료: userId={}, phoneNum={}", userId, phoneNum);
    }

    /**
     * 핸드폰번호로 일반 회원 여부 확인
     */
    public boolean isGeneralUserByPhoneNumber(String phoneNum) {
        if (phoneNum == null || phoneNum.trim().isEmpty()) {
            return false;
        }

        var userOpt = userRepository.findByPhoneNum(phoneNum);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            int snsCount = snsSignUpRepo.countByUserNo(user.getUserNo());
            return snsCount == 0; // SNS 연동이 없으면 일반 회원
        }
        return false;
    }

    /**
     * 이메일로 일반 회원 여부 확인
     */
    public boolean isGeneralUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            int snsCount = snsSignUpRepo.countByUserNo(user.getUserNo());
            return snsCount == 0; // SNS 연동이 없으면 일반 회원
        }
        return false;
    }

    private String generateSecurePassword() {
        String lower = RandomStringUtils.random(4, true, false); // 소문자
        String digit = RandomStringUtils.random(2, false, true); // 숫자
        String special = RandomStringUtils.random(2, "!@#$%&*"); // 특수문자
        return StringShuffle(lower + digit + special);
    }

    private String StringShuffle(String input) {
        List<Character> chars = input.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(chars); // 리스트 요소 무작위 섞기

        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            sb.append(c);
        }

        return sb.toString();
    }
}
