package com.swygbro.packup.register.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swygbro.packup.register.service.RegisterService;
import com.swygbro.packup.user.vo.UserVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegisterController {

    private final RegisterService registerService;

    private final BCryptPasswordEncoder encoder;

    @PostMapping("/userRegister")
    public ResponseEntity<Map<String, Object>> userRegister(@RequestBody UserVo userVo) {
        try {
            System.out.println("userVo : "+userVo);

            userVo.setUserPw(encoder.encode(userVo.getUserPw()));
            userVo.setRole("USER");

            // 회원가입 처리 로직
            registerService.registerUser(userVo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "회원가입이 성공적으로 완료되었습니다.");
            response.put("userId", userVo.getUserId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "회원가입 처리 중 오류가 발생했습니다.");
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/chkId")
    public ResponseEntity<Map<String,Object>> chkId(@RequestBody UserVo userVo){
        String userId = userVo.getUserId();

        System.out.println("userId : "+userId);

        int equalCnt = registerService.chkId(userId);

        System.out.println("equalCnt : "+equalCnt);

        Map<String, Object> response = new HashMap<>();

        if(equalCnt > 0){
            response.put("status", "fail");
            response.put("message", "동일한 아이디가 존재합니다.");
        }else{
            response.put("status", "success");
            response.put("message", "가능한 아이디입니다.");
        }

        System.out.println("response : "+response);

        return ResponseEntity.ok(response);
    }

}
