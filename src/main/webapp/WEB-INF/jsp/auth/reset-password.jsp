<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>비밀번호 재설정 - PackUp</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            margin: 0;
            padding: 20px;
        }
        
        .container {
            background: white;
            padding: 40px;
            border-radius: 10px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
            width: 100%;
            max-width: 450px;
        }
        
        .header {
            text-align: center;
            margin-bottom: 30px;
        }
        
        .header h1 {
            color: #667eea;
            margin-bottom: 10px;
            font-size: 28px;
        }
        
        .header p {
            color: #666;
            font-size: 14px;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
            color: #333;
        }
        
        .form-group input {
            width: 100%;
            padding: 12px;
            border: 1px solid #ddd;
            border-radius: 6px;
            font-size: 16px;
            box-sizing: border-box;
            transition: border-color 0.3s;
        }
        
        .form-group input:focus {
            border-color: #667eea;
            outline: none;
            box-shadow: 0 0 5px rgba(102, 126, 234, 0.3);
        }
        
        .btn {
            width: 100%;
            padding: 12px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 6px;
            font-size: 16px;
            cursor: pointer;
            transition: background-color 0.3s;
            font-weight: bold;
        }
        
        .btn:hover {
            background: #5a6fd8;
        }
        
        .btn:disabled {
            background: #ccc;
            cursor: not-allowed;
        }
        
        .message {
            padding: 12px;
            border-radius: 6px;
            margin-bottom: 20px;
            text-align: center;
        }
        
        .message.success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        
        .message.error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        
        .token-info {
            background: #e9ecef;
            padding: 12px;
            border-radius: 6px;
            margin-bottom: 20px;
            font-size: 12px;
            color: #666;
            word-break: break-all;
        }
        
        .back-link {
            text-align: center;
            margin-top: 20px;
        }
        
        .back-link a {
            color: #667eea;
            text-decoration: none;
            font-size: 14px;
        }
        
        .back-link a:hover {
            text-decoration: underline;
        }
        
        .password-requirements {
            font-size: 12px;
            color: #666;
            margin-top: 5px;
        }
        
        .loading {
            display: none;
            text-align: center;
            margin-top: 10px;
        }
        
        .loading::after {
            content: '';
            width: 20px;
            height: 20px;
            border: 2px solid #f3f3f3;
            border-top: 2px solid #667eea;
            border-radius: 50%;
            display: inline-block;
            animation: spin 1s linear infinite;
        }
        
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🔐 PackUp</h1>
            <p>비밀번호 재설정</p>
        </div>
        
        <!-- 토큰 정보 표시 (개발/테스트용) -->
        <c:if test="${not empty param.token}">
            <div class="token-info">
                <strong>토큰:</strong> <span id="tokenValue">${param.token}</span>
            </div>
        </c:if>
        
        <!-- 메시지 표시 영역 -->
        <div id="messageArea"></div>
        
        <form id="resetPasswordForm">
            <input type="hidden" id="token" name="token" value="${param.token}">
            
            <div class="form-group">
                <label for="newPassword">새 비밀번호</label>
                <input type="password" id="newPassword" name="newPassword" required 
                       placeholder="새 비밀번호를 입력하세요">
                <div class="password-requirements">
                    * 8자 이상, 영문/숫자/특수문자 조합을 권장합니다
                </div>
            </div>
            
            <div class="form-group">
                <label for="confirmPassword">비밀번호 확인</label>
                <input type="password" id="confirmPassword" name="confirmPassword" required 
                       placeholder="비밀번호를 다시 입력하세요">
            </div>
            
            <button type="submit" class="btn" id="submitBtn">비밀번호 변경</button>
            
            <div class="loading" id="loading">처리 중입니다...</div>
        </form>
        
        <div class="back-link">
            <a href="/lgn/login">← 로그인 페이지로 돌아가기</a>
        </div>
    </div>

    <script>
        $(document).ready(function() {
            // 토큰 유효성 체크
            const token = $('#token').val();
            if (!token || token.trim() === '') {
                showMessage('유효하지 않은 링크입니다. 비밀번호 재설정을 다시 요청해주세요.', 'error');
                $('#submitBtn').prop('disabled', true);
                return;
            }
            
            // 실시간 비밀번호 확인
            $('#confirmPassword').on('input', function() {
                const newPassword = $('#newPassword').val();
                const confirmPassword = $(this).val();
                
                if (confirmPassword && newPassword !== confirmPassword) {
                    $(this).css('border-color', '#dc3545');
                } else {
                    $(this).css('border-color', '#ddd');
                }
            });
            
            // 폼 제출 처리
            $('#resetPasswordForm').on('submit', function(e) {
                e.preventDefault();
                
                const newPassword = $('#newPassword').val();
                const confirmPassword = $('#confirmPassword').val();
                
                // 유효성 검사
                if (newPassword.length < 8) {
                    showMessage('비밀번호는 8자 이상이어야 합니다.', 'error');
                    $('#newPassword').focus();
                    return;
                }
                
                if (newPassword !== confirmPassword) {
                    showMessage('비밀번호가 일치하지 않습니다.', 'error');
                    $('#confirmPassword').focus();
                    return;
                }
                
                // 버튼 비활성화 및 로딩 표시
                $('#submitBtn').prop('disabled', true).text('처리 중...');
                $('#loading').show();
                
                // AJAX 요청
                $.ajax({
                    url: '/auth/reset-password',
                    type: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify({
                        token: token,
                        newPassword: newPassword
                    }),
                    success: function(response) {
                        showMessage('✅ ' + response, 'success');
                        
                        // 성공 시 5초 후 로그인 페이지로 이동
                        setTimeout(function() {
                            window.location.href = '/lgn/login';
                        }, 5000);
                        
                        // 카운트다운 표시
                        let countdown = 5;
                        const countdownInterval = setInterval(function() {
                            countdown--;
                            if (countdown > 0) {
                                showMessage('✅ ' + response + ' (' + countdown + '초 후 로그인 페이지로 이동)', 'success');
                            } else {
                                clearInterval(countdownInterval);
                            }
                        }, 1000);
                    },
                    error: function(xhr) {
                        let errorMessage = '서버 오류가 발생했습니다.';
                        if (xhr.responseText) {
                            errorMessage = xhr.responseText;
                        }
                        showMessage('❌ ' + errorMessage, 'error');
                    },
                    complete: function() {
                        $('#submitBtn').prop('disabled', false).text('비밀번호 변경');
                        $('#loading').hide();
                    }
                });
            });
        });
        
        function showMessage(message, type) {
            const messageArea = $('#messageArea');
            messageArea.html('<div class="message ' + type + '">' + message + '</div>');
            
            // 에러 메시지가 아닌 경우 10초 후 자동 제거
            if (type !== 'error') {
                setTimeout(function() {
                    messageArea.empty();
                }, 10000);
            }
        }
    </script>
</body>
</html>