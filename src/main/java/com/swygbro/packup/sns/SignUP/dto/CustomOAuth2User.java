package com.swygbro.packup.sns.SignUP.dto;

import com.swygbro.packup.sns.Helper.socialLoginType;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.lang.*;
import java.util.*;

@Getter
public class CustomOAuth2User implements OAuth2User {
    // ✅ 추가 필드
    private final boolean isNewUser;             // 신규 SNS 로그인(아직 가입/연동 전) 여부
    private final String socialId;               // “kakao_123”, “naver_abc”, “google_456” 등
    private final String email;


    private final String userId;
    private final String userNm;
    private final String nameKey;
    private final int userNo;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;
    private final socialLoginType socialLoginType;

    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes,
                            String userId,
                            String userNm,
                            String nameKey,
                            int userNo,
                            socialLoginType socialLoginType,
                            boolean isNewUser,
                            String socialId,
                            String email) {
        this.authorities = authorities;
        this.attributes = attributes;
        this.userId = userId;
        this.userNm = userNm;
        this.nameKey = nameKey;
        this.userNo = userNo;
        this.socialLoginType = socialLoginType;
        this.isNewUser = isNewUser;
        this.socialId = socialId;
        this.email = email;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {

        return userId;
    }

    // SNS 식별 ID
    public String getSocialId() {
        Object id = attributes.get("id");
        if (id == null && attributes.get("response") instanceof Map<?, ?> response) {
            id = response.get("id");
        } else if (id == null && attributes.get("sub") != null) {
            id = attributes.get("sub");
        }
        return id != null ? id.toString() : null;
    }

    // enum 기반 반환
    public socialLoginType getSocialLoginType() {
        return socialLoginType;
    }

    // sns 로그인시 newUser 처리
    public static CustomOAuth2User newUser(Collection<? extends GrantedAuthority> authorities,
                                           Map<String, Object> attributes,
                                           socialLoginType type,
                                           String email,
                                           String socialId) {
        return new CustomOAuth2User(authorities, attributes, null, null, "name", 0, type,
                true, socialId, email);
    }

}
