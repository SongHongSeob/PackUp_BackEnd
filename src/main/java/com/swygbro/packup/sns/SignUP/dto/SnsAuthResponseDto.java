package com.swygbro.packup.sns.SignUP.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class SnsAuthResponseDto {
    private String userId;      // 사용자 ID (name)
    private String userNm;      // 사용자 이름
    private String email;       // 이메일
    private String socialId;    // SNS ID
    private String loginType;   // 로그인 타입 (KAKAO, NAVER, GOOGLE)
    private boolean needsAdditionalInfo; // 추가 정보 입력 필요 여부 (전화번호, 비밀번호)
}
