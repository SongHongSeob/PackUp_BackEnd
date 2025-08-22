<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>마이페이지</title>
    <!-- jQuery 라이브러리 추가 -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            max-width: 600px;
            margin: 0 auto;
            background-color: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .header {
            text-align: center;
            margin-bottom: 30px;
        }
        .header h1 {
            color: #333;
            margin-bottom: 10px;
        }
        .form-group {
            margin-bottom: 20px;
        }
        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #555;
        }
        .form-group input {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 16px;
            box-sizing: border-box;
        }
        .form-group input:disabled {
            background-color: #f9f9f9;
            color: #666;
        }
        .btn {
            padding: 12px 30px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
            margin: 5px;
            font-size: 16px;
        }
        .btn-primary {
            background-color: #007bff;
            color: white;
        }
        .btn-primary:hover {
            background-color: #0056b3;
        }
        .btn-secondary {
            background-color: #6c757d;
            color: white;
        }
        .btn-secondary:hover {
            background-color: #545b62;
        }
        .message {
            padding: 10px;
            margin-bottom: 20px;
            border-radius: 4px;
        }
        .message.success {
            background-color: #d4edda;
            border: 1px solid #c3e6cb;
            color: #155724;
        }
        .message.error {
            background-color: #f8d7da;
            border: 1px solid #f5c6cb;
            color: #721c24;
        }
        .button-group {
            text-align: center;
            margin-top: 30px;
        }
        .btn:disabled {
            background-color: #cccccc;
            cursor: not-allowed;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>마이페이지</h1>
            <p>회원 정보 수정</p>
        </div>

        <!-- 메시지 표시 -->
        <c:if test="${not empty message}">
            <div class="message success">${message}</div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="message error">${error}</div>
        </c:if>

        <form id="updateForm">
            <div class="form-group">
                <label for="userId">아이디</label>
                <input type="text" id="userId" name="userId" value="${userInfo.userId}" disabled>
            </div>

            <div class="form-group">
                <label for="userPw">비밀번호</label>
                <input type="password" id="userPw" name="userPw" placeholder="변경하지 않으려면 공백으로 두세요">
            </div>

            <div class="form-group">
                <label for="userNm">이름</label>
                <input type="text" id="userNm" name="userNm" value="${userInfo.userNm}" required>
            </div>

            <div class="form-group">
                <label for="email">이메일</label>
                <input type="email" id="email" name="email" value="${userInfo.email}" required>
            </div>

            <div class="form-group">
                <label for="phoneNum">전화번호</label>
                <input type="tel" id="phoneNum" name="phoneNum" value="${userInfo.phoneNum}" required>
            </div>

            <div class="button-group">
                <button type="submit" class="btn btn-primary" id="updateBtn">정보 수정</button>
                <a href="/dashboard/dashboard" class="btn btn-secondary">대시보드로 돌아가기</a>
            </div>
        </form>
    </div>

    <script>
        $(document).ready(function() {
            // Form 제출 이벤트 처리
            $('#updateForm').on('submit', function(e) {
                e.preventDefault(); // 기본 제출 방지
                updateUser();
            });
        });

        function updateUser() {
            // 버튼 비활성화
            $('#updateBtn').prop('disabled', true).text('처리 중...');
            
            var formData = {
                userId: $('#userId').val(),
                userNm: $('#userNm').val(),
                email: $('#email').val(),
                phoneNum: $('#phoneNum').val(),
                userPw: $('#userPw').val()
            };
            
            $.ajax({
                url: '/mypage/updateUser', // URL 통일
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(formData),
                success: function(response) {
                    if (response.success) {
                        alert(response.message);
                        window.location.href = '/mypage/mypage';
                    } else {
                        alert('오류: ' + response.message);
                    }
                },
                error: function(xhr, status, error) {
                    console.error('Error:', error);
                    alert('서버 오류가 발생했습니다.');
                },
                complete: function() {
                    // 버튼 다시 활성화
                    $('#updateBtn').prop('disabled', false).text('정보 수정');
                }
            });
        }
    </script>
</body>
</html>