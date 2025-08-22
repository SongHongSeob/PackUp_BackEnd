package com.swygbro.packup.user.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        System.out.println("resetUrl : "+resetUrl);
        
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("비밀번호 재설정");
            
            String htmlContent = 
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }" +
                ".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }" +
                ".btn { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; font-weight: bold; margin: 20px 0; }" +
                ".btn:hover { background: #5a6fd8; }" +
                ".footer { color: #666; font-size: 12px; margin-top: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>PackUp</h1>" +
                "<h2>비밀번호 재설정</h2>" +
                "</div>" +
                "<div class='content'>" +
                "<p>안녕하세요.</p>" +
                "<p>비밀번호 재설정을 요청하셨습니다.<br>" +
                "아래 버튼을 클릭하여 새로운 비밀번호를 설정해주세요.</p>" +
                "<div style='text-align: center;'>" +
                "<a href='" + resetUrl + "' class='btn'>비밀번호 재설정하기</a>" +
                "</div>" +
                "<p><strong>중요:</strong></p>" +
                "<ul>" +
                "<li>본 링크는 1시간 후 만료됩니다.</li>" +
                "<li>요청하지 않으셨다면 이 메일을 무시해주세요.</li>" +
                "<li>보안을 위해 링크를 다른 사람과 공유하지 마세요.</li>" +
                "</ul>" +
                "<div class='footer'>" +
                "<p>링크가 작동하지 않으면 아래 URL을 복사하여 브라우저에 직접 입력하세요:</p>" +
                "<p style='word-break: break-all; background: #eee; padding: 10px; border-radius: 4px;'>" + resetUrl + "</p>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
            
            helper.setText(htmlContent, true); // true = HTML 형식
            
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}