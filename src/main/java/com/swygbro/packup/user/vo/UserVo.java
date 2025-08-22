package com.swygbro.packup.user.vo;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVo {
    private int userNo;
    private String userId;
    private String userPw;
    private String email;
    private String userNm;
    private String phoneNum;
    private String gender;
    private Date lastLoginDt;
    private Date regDt;
    private String regId;
    private Date updDt;
    private String updId;
    private String role;
    private char useYn;
    private char delYn;
    private int lgnFailCnt;
    private String personalInfoAcq;
    private String infoAcq;
    private String mktAgree;
}
