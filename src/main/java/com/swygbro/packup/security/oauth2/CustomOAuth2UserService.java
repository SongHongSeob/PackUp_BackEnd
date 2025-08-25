package com.swygbro.packup.security.oauth2;

import com.swygbro.packup.sns.Helper.socialLoginType;
import com.swygbro.packup.sns.SignUP.entity.SnsUser;
import com.swygbro.packup.sns.SignUP.repository.SnsSignUpRepo;
import com.swygbro.packup.user.entity.User;
import com.swygbro.packup.user.repository.UserRepository;
import com.swygbro.packup.sns.SignUP.dto.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User>{

    private final UserRepository userRepository;
    private final SnsSignUpRepo snsSignUpRepo;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // ex: "kakao"
        socialLoginType socialType = socialLoginType.valueOf(registrationId.toUpperCase());

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String socialId = extractSocialId(attributes, socialType);
        String email = extractEmail(attributes, socialType);
        String phoneNum = extractPhoneNumber(attributes, socialType);

        log.info("SNS 로그인 시도: socialType={}, socialId={}, email={}, phoneNum={}", 
                socialType, socialId, email, phoneNum);

        User user = null;

        // 1. 기존 SNS 사용자인지 확인
        Optional<SnsUser> existingSnsUser = snsSignUpRepo.findBySocialIdAndLoginType(socialId, socialType.name());
        if (existingSnsUser.isPresent()) {
            // 기존 SNS 사용자 - 연결된 User 정보 조회
            user = userRepository.findByUserNo(existingSnsUser.get().getUserNo())
                    .orElseThrow(() -> new RuntimeException("연결된 사용자 정보를 찾을 수 없습니다."));
            
            log.info("기존 SNS 사용자 로그인: userId={}, userNo={}", user.getUserId(), user.getUserNo());
        }
        else {
            // 신규 SNS 사용자 - 가입 없이 임시 사용자 객체 반환
            log.info("신규 SNS 사용자 감지: socialType={}, socialId={}, email={}", socialType, socialId, email);
            
            // 임시 사용자 정보로 CustomOAuth2User 반환 (가입은 OAuth2SuccessHandler에서 처리)
            return new CustomOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    email != null ? email : (socialType.name().toLowerCase() + "_" + extractIdFromSocialId(socialId)),
                    "신규사용자",
                    "name",
                    0,  // 임시 userNo
                    socialType,
                    true,  // 신규 사용자 플래그
                    socialId,
                    email
            );
        }

        String role = user != null ? user.getRole() : "ROLE_USER";
        if(role == null || role.trim().isEmpty()){
            role = "ROLE_USER";
        }

        // 기존 사용자인 경우에만 실제 사용자 정보로 반환
        return new CustomOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(role)),
                attributes,
                user.getUserId(),
                user.getUserNm(),
                "name",
                user.getUserNo(),
                socialType,
                false,  // 기존 사용자
                socialId,
                email
        );
    }

    /**
     * 신규 SNS 사용자 처리 로직

    private User handleNewSnsUser(socialLoginType socialType, String socialId, String email, String phoneNum) {
        log.info("신규 SNS 사용자 처리 시작: socialType={}, email={}, phoneNum={}", socialType, email, phoneNum);

        // 1. 이메일 기반 일반 회원 중복 체크 (네이버, 구글)
        if (email != null && !email.trim().isEmpty()) {
            Optional<User> existingUserByEmail = userRepository.findByEmail(email);
            if (existingUserByEmail.isPresent()) {
                User existingUser = existingUserByEmail.get();
                
                // 일반 회원인지 확인 (SNS 연동 정보가 없으면 일반 회원)
                int snsCount = snsSignUpRepo.countByUserNo(existingUser.getUserNo());
                if (snsCount == 0) {
                    log.warn("이미 일반 회원으로 가입된 이메일입니다: email={}", email);
                    throw new IllegalStateException("이미 일반 회원으로 가입된 이메일입니다. 핸드폰번호 인증을 통해 계정을 연동해주세요.");
                } else {
                    // 이미 다른 SNS로 연동된 사용자 - 다중 연동 차단
                    log.warn("이미 다른 SNS로 가입된 이메일입니다: email={}, 기존 사용자: userNo={}", email, existingUser.getUserNo());
                    throw new IllegalStateException("이미 다른 SNS 계정으로 가입된 이메일입니다. 기존에 가입하신 SNS 계정으로 로그인해주세요.");
                }
            }
        }

        // 2. 핸드폰번호 기반 일반 회원 중복 체크 (추가 정보 입력 후 체크를 위한 준비)
        // 현재는 SNS 최초 로그인 시 핸드폰번호가 없으므로, 추가 정보 입력 시 체크

        // 3. 신규 SNS 사용자 자동 가입
        return createNewSnsUser(socialType, socialId, email);
    }

    /**
     * 신규 SNS 사용자 생성

    private User createNewSnsUser(socialLoginType socialType, String socialId, String email) {
        String userId;
        
        // 카카오는 이메일이 없을 수 있으므로 소셜ID 기반으로 생성
        if (socialType == socialLoginType.KAKAO || email == null || email.trim().isEmpty()) {
            userId = socialType.name().toLowerCase() + "_" + extractIdFromSocialId(socialId);
        } else {
            userId = email;
        }

        JoinDto dto = JoinDto.builder()
                .USER_ID(userId)
                .USER_NM("packUp#" + UUID.randomUUID().toString().substring(0, 6))
                .LOGIN_TYPE(socialType.name())
                .EMAIL(email)
                .USER_PW(generateRandomPassword())
                .SOCIAL_ID(socialId)
                .build();

        log.info("신규 SNS 사용자 생성: userId={}, socialType={}", userId, socialType);
        
        try {
            joinService.joinSocial(dto);
            return userRepository.findByUserId(dto.getUSER_ID())
                    .orElseThrow(() -> new RuntimeException("생성된 사용자 정보를 찾을 수 없습니다."));
        } catch (Exception e) {
            log.error("SNS 사용자 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("SNS 회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
     */

    /**
     * 핸드폰번호로 일반 회원 중복 체크 (추가 정보 입력 시 사용)
     */
    public void validatePhoneNumberForSnsUser(String phoneNum, String snsUserId) {
        if (phoneNum == null || phoneNum.trim().isEmpty()) {
            return; // 핸드폰번호가 없으면 체크하지 않음
        }

        Optional<User> existingUserByPhone = userRepository.findByPhoneNum(phoneNum);
        if (existingUserByPhone.isPresent()) {
            User existingUser = existingUserByPhone.get();
            
            // 현재 SNS 사용자인지 확인
            if (!existingUser.getUserId().equals(snsUserId)) {
                // 다른 사용자의 핸드폰번호
                int snsCount = snsSignUpRepo.countByUserNo(existingUser.getUserNo());
                if (snsCount == 0) {
                    // 일반 회원의 핸드폰번호
                    log.warn("이미 일반 회원으로 가입된 핸드폰번호입니다: phoneNum={}", phoneNum);
                    throw new IllegalStateException("이미 일반 회원으로 가입된 핸드폰번호입니다. 다른 번호를 사용해주세요.");
                } else {
                    // 다른 SNS 사용자의 핸드폰번호 - SNS 다중 연동 방지
                    log.warn("이미 다른 SNS 사용자가 사용 중인 핸드폰번호입니다: phoneNum={}", phoneNum);
                    throw new IllegalStateException("이미 다른 SNS 계정으로 가입된 핸드폰번호입니다. 각 SNS 계정마다 고유한 핸드폰번호를 사용해주세요.");
                }
            }
        }
    }

    private String generateRandomPassword() {
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specials = "!@#$%^&*";
        String all = lowerCase + numbers + specials;

        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        // 필수 구성 요소 1개씩 삽입
        sb.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        sb.append(numbers.charAt(random.nextInt(numbers.length())));
        sb.append(specials.charAt(random.nextInt(specials.length())));

        // 나머지 랜덤 문자로 채움 (총 8자 이상)
        for (int i = 0; i < 5; i++) {
            sb.append(all.charAt(random.nextInt(all.length())));
        }

        // 문자열 섞기 (순서 보안)
        List<Character> chars = new ArrayList<>();
        for (char c : sb.toString().toCharArray()) {
            chars.add(c);
        }
        Collections.shuffle(chars);

        StringBuilder finalPw = new StringBuilder();
        for (char c : chars) {
            finalPw.append(c);
        }

        return finalPw.toString();
    }

    private String extractSocialId(Map<String, Object> attributes, socialLoginType type) {
        if (type == socialLoginType.KAKAO) {
            return "kakao_" + attributes.get("id");
        } else if (type == socialLoginType.NAVER) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            return "naver_" + response.get("id");
        } else if (type == socialLoginType.GOOGLE) {
            return "google_" + attributes.get("sub");
        }
        throw new RuntimeException("지원하지 않는 소셜 로그인 타입입니다.");
    }

    private String extractEmail(Map<String, Object> attributes, socialLoginType type) {
        try {
            if (type == socialLoginType.KAKAO) {
                Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
                return account != null ? (String) account.get("email") : null;
            } else if (type == socialLoginType.NAVER) {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                return response != null ? (String) response.get("email") : null;
            } else if (type == socialLoginType.GOOGLE) {
                return (String) attributes.get("email");
            }
        } catch (Exception e) {
            log.warn("이메일 추출 실패: socialType={}, error={}", type, e.getMessage());
        }
        return null;
    }

    private String extractPhoneNumber(Map<String, Object> attributes, socialLoginType type) {
        try {
            if (type == socialLoginType.NAVER) {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                return response != null ? (String) response.get("mobile") : null;
            }
            // 카카오와 구글은 기본적으로 핸드폰번호를 제공하지 않음
        } catch (Exception e) {
            log.warn("핸드폰번호 추출 실패: socialType={}, error={}", type, e.getMessage());
        }
        return null;
    }

    private String extractIdFromSocialId(String socialId) {
        if (socialId.contains("_")) {
            return socialId.substring(socialId.lastIndexOf("_") + 1);
        }
        return socialId;
    }
}
