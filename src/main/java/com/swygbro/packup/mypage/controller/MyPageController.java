package com.swygbro.packup.mypage.controller;

import java.util.HashMap;
import java.util.Map;

import com.swygbro.packup.sns.Helper.socialLoginType;
import com.swygbro.packup.sns.SignUP.entity.SnsUser;
import com.swygbro.packup.sns.SignUP.repository.SnsSignUpRepo;
import com.swygbro.packup.user.entity.User;
import com.swygbro.packup.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.swygbro.packup.user.service.UserService;
import com.swygbro.packup.user.vo.UserVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SnsSignUpRepo snsSignUpRepo;

    @PostMapping("/mypage")
    public ModelAndView mypage(Authentication authentication) {
        String userId = authentication.getName();
        log.info("MyPage accessed by user: {}", userId);
        
        UserVo userInfo = userService.getUserInfo(userId);
        if (userInfo != null) {
            userInfo.setUserPw("");
        }
        
        ModelAndView mv = new ModelAndView("mypage/mypage");
        mv.addObject("userInfo", userInfo);
        return mv;
    }

    @PostMapping("/updateUser")
    public ResponseEntity<Map<String, Object>> updateUser(@RequestBody UserVo userVo, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String userId = authentication.getName();
            userVo.setUpdId(userId);
            
            if (userVo.getUserPw() != null && !userVo.getUserPw().trim().isEmpty()) {
                userVo.setUserPw(passwordEncoder.encode(userVo.getUserPw()));
            } else {
                userVo.setUserPw(null);
            }
            
            int result = userService.updateUser(userVo);
            
            if (result > 0) {
                response.put("success", true);
                response.put("message", "정보가 성공적으로 수정되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "정보 수정에 실패했습니다.");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating user info: ", e);
            response.put("success", false);
            response.put("message", "정보 수정 중 오류가 발생했습니다.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/sns/disconnect/{snsType}")
    public ResponseEntity<String> disconnectSns(@PathVariable("snsType") socialLoginType snsType,
                                                Authentication authentication) {
        String userId = authentication.getName();
        User user = (User) userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 해당 연동 정보 조회
        SnsUser snsUser = (SnsUser) snsSignUpRepo.findByUserNoAndLoginType(user.getUserNo(), String.valueOf(snsType))
                .orElseThrow(() -> new RuntimeException("해당 SNS 연동 정보가 없습니다."));

        // 연동이 유일한 로그인 수단이라면 해제 불가
        int linkedCount = snsSignUpRepo.countByUserNo(user.getUserNo());
        if (linkedCount <= 1) {
            return ResponseEntity.badRequest().body("최소 하나의 로그인 수단은 유지되어야 합니다.");
        }

        snsSignUpRepo.delete(snsUser);
        return ResponseEntity.ok("SNS 연동 해제 완료");
    }


}