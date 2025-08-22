package com.swygbro.packup.user.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.swygbro.packup.config.CustomUserDetails;
import com.swygbro.packup.user.Mapper.UserMapper;
import com.swygbro.packup.user.entity.User;
import com.swygbro.packup.user.entity.PasswordResetToken;
import com.swygbro.packup.user.repository.UserRepository;
import com.swygbro.packup.user.repository.PasswordResetTokenRepository;
import com.swygbro.packup.user.vo.UserVo;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    public int insertUser(UserVo userDto){
        //패스워드 암호화
        userDto.setUserPw(passwordEncoder.encode(userDto.getUserPw()));
        return userMapper.insertUser(userDto);
    }

    public UserVo getUserInfo(String userId) {
        return userMapper.getUserInfo(userId);
    }

    public int updateUser(UserVo userVo) {
        return userMapper.updateUser(userVo);
    }

    public void sendPasswordResetEmail(String userId) {
        UserVo user = userMapper.getUserByEmail(userId);
        if (user == null) {
            throw new RuntimeException("해당 이메일로 등록된 사용자가 없습니다.");
        }

        Optional<PasswordResetToken> existingToken = passwordResetTokenRepository.findByEmailAndUsedFalse(userId);
        if (existingToken.isPresent()) {
            passwordResetTokenRepository.delete(existingToken.get());
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);
        
        PasswordResetToken resetToken = new PasswordResetToken(token, userId, expiryDate);
        passwordResetTokenRepository.save(resetToken);
        
        emailService.sendPasswordResetEmail(userId, token);
    }

    public boolean validateResetToken(String token) {
        try {
            Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByTokenAndUsedFalse(token);
            
            if (tokenOpt.isEmpty()) {
                return false;
            }
            
            PasswordResetToken resetToken = tokenOpt.get();
            
            if (resetToken.isExpired()) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByTokenAndUsedFalse(token);
        
        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        
        if (resetToken.isExpired()) {
            throw new RuntimeException("토큰이 만료되었습니다.");
        }
        
        UserVo user = userMapper.getUserByEmail(resetToken.getEmail());
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        user.setUserPw(passwordEncoder.encode(newPassword));
        userMapper.updateUser(user);
        
        passwordResetTokenRepository.markTokenAsUsed(token);
    }

    public UserVo getUserByEmail(String email) {
        return userMapper.getUserByEmail(email);
    }

    public UserVo authenticateUser(String userId, String rawPassword) {
        try {
            // Get user details from database
            CustomUserDetails userDetails = userMapper.selectUserById(userId);
            
            if (userDetails == null) {
                return null; // User not found
            }
            
            // Check if user is enabled
            if (!userDetails.isEnabled()) {
                return null; // User is disabled
            }
            
            // Verify password
            if (!passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
                return null; // Invalid password
            }
            
            // Get full user info and return as UserVo
            UserVo userVo = userMapper.getUserInfo(userId);
            return userVo;
            
        } catch (Exception e) {
            System.err.println("Authentication error for user: " + userId + ", Error: " + e.getMessage());
            return null;
        }
    }

}