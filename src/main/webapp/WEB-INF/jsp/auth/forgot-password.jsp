<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>비밀번호 찾기 - PackUp</title>
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
            max-width: 400px;
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
            line-height: 1.5;
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
            <p>비밀번호를 잊으셨나요?<br>
            가입 시 등록한 아이디를 입력하시면<br>
            이메일로 재설정 링크를 보내드립니다.</p>
        </div>
        
        <!-- 메시지 표시 영역 -->
        <div id="messageArea"></div>
        
        <form id="forgotPasswordForm">
            <div class="form-group">
                <label for="userId">아이디</label>
                <input type="text" id="userId" name="userId" required 
                       placeholder="가입 시 등록한 아이디를 입력하세요">
            </div>
            
            <button type="submit" class="btn" id="submitBtn">재설정 링크 보내기</button>
            
            <div class="loading" id="loading">이메일을 발송하고 있습니다...</div>
        </form>
        
        <div class="back-link">
            <a href="/lgn/login">← 로그인 페이지로 돌아가기</a>
        </div>
    </div>

    <script>
        $(document).ready(function() {
            $('#forgotPasswordForm').on('submit', function(e) {
                e.preventDefault();
                
                const userId = $('#userId').val().trim();
                
                if (!userId) {
                    showMessage('아이디를 입력해주세요.', 'error');
                    $('#userId').focus();
                    return;
                }
                
                // 버튼 비활성화 및 로딩 표시
                $('#submitBtn').prop('disabled', true).text('발송 중...');
                $('#loading').show();
                
                // AJAX 요청
                $.ajax({
                    url: '/auth/forgot-password',
                    type: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify({
                        userId: userId
                    }),
                    success: function(response) {
                        showMessage('✅ ' + response + '<br><br>📧 이메일을 확인하고 링크를 클릭해주세요!', 'success');
                        $('#userId').val(''); // 입력 필드 초기화
                    },
                    error: function(xhr) {
                        let errorMessage = '서버 오류가 발생했습니다.';
                        if (xhr.responseText) {
                            errorMessage = xhr.responseText;
                        }
                        showMessage('❌ ' + errorMessage, 'error');
                    },
                    complete: function() {
                        $('#submitBtn').prop('disabled', false).text('재설정 링크 보내기');
                        $('#loading').hide();
                    }
                });
            });
        });
        
        function showMessage(message, type) {
            const messageArea = $('#messageArea');
            messageArea.html('<div class="message ' + type + '">' + message + '</div>');
            
            // 성공 메시지가 아닌 경우 5초 후 자동 제거
            if (type === 'error') {
                setTimeout(function() {
                    messageArea.empty();
                }, 5000);
            }
        }
    </script>
</body>
</html>