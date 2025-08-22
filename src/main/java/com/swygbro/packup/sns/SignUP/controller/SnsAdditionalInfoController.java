package com.swygbro.packup.sns.SignUP.controller;

import com.swygbro.packup.sns.SignUP.Service.JoinService;
import com.swygbro.packup.sns.SignUP.dto.SnsAdditionalInfoDto;
import com.swygbro.packup.sns.SignUP.dto.SnsAuthResponseDto;
import com.swygbro.packup.security.jwt.JwtUtill;
import com.swygbro.packup.sns.SignUP.repository.SnsSignUpRepo;
import com.swygbro.packup.user.entity.User;
import com.swygbro.packup.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/sns")
@RequiredArgsConstructor
@Tag(name = "SNS 추가 정보 관리", description = "SNS 로그인 후 추가 정보 입력 및 관리 API")
public class SnsAdditionalInfoController {

    private final JoinService joinService;
    private final UserRepository userRepository;
    private final SnsSignUpRepo  snsSignUpRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtill jwtUtil;

    /**
     * SNS 로그인 후 추가 정보 입력 (전화번호, 비밀번호)
     */
    @Operation(summary = "SNS 사용자 추가 정보 입력", 
               description = "SNS 로그인 후 전화번호와 비밀번호를 추가로 입력하여 회원가입을 완료합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "추가 정보 저장 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "중복된 정보 (핸드폰번호 등)"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/additional-info")
    public ResponseEntity<Map<String, Object>> updateAdditionalInfo(
            @Parameter(description = "추가 정보 입력 데이터", required = true)
            @Valid @RequestBody SnsAdditionalInfoDto dto,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // ✅ JWT에서 사용자 정보 추출 (선택적)
            String token = jwtUtil.extractTokenFromRequest(request);
            String userIdFromToken = null;
            if (token != null && jwtUtil.validateToken(token)) {
                userIdFromToken = jwtUtil.getUserIdFromToken(token);
            }
            
            // ✅ 토큰의 사용자 ID와 요청의 사용자 ID 일치 확인 (보안)
            if (userIdFromToken != null && !userIdFromToken.equals(dto.getUserId())) {
                response.put("success", false);
                response.put("message", "권한이 없습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // ✅ JoinService의 개선된 메서드 사용
            joinService.updateSnsUserAdditionalInfo(dto.getUserId(), dto.getPhoneNum(), dto.getPassword());
            
            // ✅ 업데이트된 사용자 정보 조회
            User user = userRepository.findByUserId(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            log.info("SNS 사용자 추가 정보 업데이트 완료: userId={}, phoneNum={}", 
                    dto.getUserId(), dto.getPhoneNum());
            
            response.put("success", true);
            response.put("message", "추가 정보가 성공적으로 저장되었습니다.");
            response.put("userId", user.getUserId());
            response.put("userNm", user.getUserNm());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            // 중복 정보 등의 비즈니스 로직 오류
            log.warn("SNS 추가 정보 업데이트 실패 (비즈니스 로직 오류): {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            
        } catch (IllegalArgumentException e) {
            // 잘못된 인자
            log.warn("SNS 추가 정보 업데이트 실패 (잘못된 인자): {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            log.error("SNS 추가 정보 업데이트 실패: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "추가 정보 저장에 실패했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * JWT 토큰에서 사용자 정보 조회
     */
    @Operation(summary = "JWT 토큰으로 사용자 정보 조회", 
               description = "JWT 토큰을 통해 로그인된 사용자의 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfoFromToken(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // ✅ JWT에서 사용자 정보 추출
            String token = jwtUtil.extractTokenFromRequest(request);
            if (token == null || !jwtUtil.validateToken(token)) {
                response.put("success", false);
                response.put("message", "유효하지 않은 토큰입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            
            // ✅ 사용자 정보 조회
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            // ✅ 추가 정보 입력 필요 여부 확인
            boolean needsAdditionalInfo = user.getPhoneNum() == null || 
                                         user.getPhoneNum().trim().isEmpty() ||
                                         user.getUserPw() == null || 
                                         user.getUserPw().trim().isEmpty();
            
            // ✅ 응답 데이터 생성
            SnsAuthResponseDto responseDto = SnsAuthResponseDto.builder()
                    .userId(userId)
                    .userNm(username)
                    .email(user.getEmail())
                    .needsAdditionalInfo(needsAdditionalInfo)
                    .build();
            
            response.put("success", true);
            response.put("data", responseDto);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "사용자 정보 조회에 실패했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 사용자 추가 정보 입력 필요 여부 확인
     */
    @Operation(summary = "추가 정보 입력 필요 여부 확인", 
               description = "특정 사용자의 추가 정보(전화번호, 비밀번호) 입력 필요 여부를 확인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "확인 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/check-additional-info/{userId}")
    public ResponseEntity<Map<String, Object>> checkAdditionalInfoNeeded(
            @Parameter(description = "확인할 사용자 ID", required = true)
            @PathVariable String userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // ✅ 사용자 조회
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            // ✅ 추가 정보 입력 필요 여부 확인
            boolean needsPhoneNumber = user.getPhoneNum() == null || user.getPhoneNum().trim().isEmpty();
            boolean needsPassword = user.getUserPw() == null || user.getUserPw().trim().isEmpty();
            boolean needsAdditionalInfo = needsPhoneNumber || needsPassword;
            
            response.put("success", true);
            response.put("needsAdditionalInfo", needsAdditionalInfo);
            response.put("needsPhoneNumber", needsPhoneNumber);
            response.put("needsPassword", needsPassword);
            response.put("userId", userId);
            response.put("userNm", user.getUserNm());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("추가 정보 필요 여부 확인 실패: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "확인에 실패했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 핸드폰번호 중복 확인 (일반 회원과의 중복 체크)
     */
    @Operation(summary = "핸드폰번호 중복 확인", 
               description = "SNS 사용자가 입력한 핸드폰번호가 일반 회원과 중복되는지 확인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "확인 성공"),
        @ApiResponse(responseCode = "409", description = "중복된 핸드폰번호"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/validate-phone")
    public ResponseEntity<Map<String, Object>> validatePhoneNumber(
            @Parameter(description = "핸드폰번호 검증 요청", required = true)
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String phoneNum = request.get("phoneNum");
            String userId = request.get("userId");
            
            if (phoneNum == null || phoneNum.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "핸드폰번호를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (userId == null || userId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "사용자 ID가 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // ✅ 일반 회원 중복 체크
            boolean isGeneralUser = joinService.isGeneralUserByPhoneNumber(phoneNum);
            if (isGeneralUser) {
                response.put("success", false);
                response.put("available", false);
                response.put("message", "이미 일반 회원으로 가입된 핸드폰번호입니다. 다른 번호를 사용해주세요.");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            // ✅ 다른 SNS 사용자와의 중복 체크
            var existingUser = userRepository.findByPhoneNum(phoneNum);
            if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
                // 다른 사용자의 핸드폰번호인지 확인
                User otherUser = existingUser.get();
                int snsCount = snsSignUpRepo.countByUserNo(otherUser.getUserNo());
                
                if (snsCount == 0) {
                    // 일반 회원의 핸드폰번호
                    response.put("success", false);
                    response.put("available", false);
                    response.put("message", "이미 일반 회원으로 가입된 핸드폰번호입니다. 다른 번호를 사용해주세요.");
                } else {
                    // 다른 SNS 사용자의 핸드폰번호
                    response.put("success", false);
                    response.put("available", false);
                    response.put("message", "이미 다른 SNS 계정으로 가입된 핸드폰번호입니다. 각 SNS 계정마다 고유한 핸드폰번호를 사용해주세요.");
                }
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            response.put("success", true);
            response.put("available", true);
            response.put("message", "사용 가능한 핸드폰번호입니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("핸드폰번호 검증 실패: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "검증 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
