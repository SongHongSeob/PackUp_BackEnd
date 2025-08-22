package com.swygbro.packup.sns.SignUP.dto;

import com.swygbro.packup.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class JoinDto {

    private String SOCIAL_ID;
    private String LOGIN_TYPE;
    private String USER_ID;
    private String USER_NO;
    private String USER_NM;
    private String USER_PW;
    private String EMAIL;

}
