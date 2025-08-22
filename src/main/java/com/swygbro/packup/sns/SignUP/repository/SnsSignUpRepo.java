package com.swygbro.packup.sns.SignUP.repository;

import com.swygbro.packup.sns.SignUP.entity.SnsUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SnsSignUpRepo extends JpaRepository<SnsUser, Long> {

    // SNS ID와 로그인 타입으로 중복 확인
    boolean existsBySocialIdAndLoginType(String socialId, String loginType);

    // SNS ID와 로그인 타입으로 조회
    Optional<SnsUser> findBySocialIdAndLoginType(String socialId, String loginType);

    // 사용자 번호로 SNS 연동 개수 확인
    int countByUserNo(int userNo);

    // 사용자 번호와 로그인 타입으로 존재 여부 확인
    boolean existsByUserNoAndLoginType(int userNo, String loginType);

    // 사용자 번호와 로그인 타입으로 조회
    Optional<SnsUser> findByUserNoAndLoginType(int userNo, String loginType);
}
