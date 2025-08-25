package com.swygbro.packup.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.swygbro.packup.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUserId(String userId);
    boolean existsByUserNo(int userNo);

    Optional<User> findByEmail(String email);

    Optional<User> findByUserId(String userId);
    
    Optional<User> findByUserNo(int userNo);
    
    // 핸드폰번호로 조회 (일반회원 중복 체크용)
    Optional<User> findByPhoneNum(String phoneNum);
    
    boolean existsByPhoneNum(String phoneNum);
}
