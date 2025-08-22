<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!-- jQuery 3.6.0 CDN -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>회원가입</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f5f5f5;
            padding: 20px;
        }
        
        .register-container {
            max-width: 500px;
            margin: 0 auto;
            background: white;
            padding: 40px;
            border-radius: 10px;
            box-shadow: 0 0 20px rgba(0,0,0,0.1);
        }
        
        h2 {
            text-align: center;
            margin-bottom: 30px;
            color: #333;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        label {
            display: block;
            margin-bottom: 5px;
            color: #555;
            font-weight: bold;
        }
        
        input[type="text"], 
        input[type="email"], 
        input[type="password"], 
        input[type="tel"], 
        select, 
        textarea {
            width: 100%;
            padding: 12px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 16px;
            transition: border-color 0.3s;
        }
        
        input:focus, select:focus, textarea:focus {
            outline: none;
            border-color: #4CAF50;
        }
        
        .gender-group {
            display: flex;
            gap: 20px;
            margin-top: 5px;
        }
        
        .gender-option {
            display: flex;
            align-items: center;
            gap: 5px;
        }
        
        .checkbox-group {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-top: 5px;
        }
        
        .submit-btn {
            width: 100%;
            padding: 15px;
            background-color: #4CAF50;
            color: white;
            border: none;
            border-radius: 5px;
            font-size: 18px;
            cursor: pointer;
            transition: background-color 0.3s;
        }
        
        .submit-btn:hover {
            background-color: #45a049;
        }
        
        .cancel-btn {
            width: 100%;
            padding: 15px;
            background-color: #f44336;
            color: white;
            border: none;
            border-radius: 5px;
            font-size: 18px;
            cursor: pointer;
            transition: background-color 0.3s;
            margin-top: 10px;
        }
        
        .cancel-btn:hover {
            background-color: #da190b;
        }
        
        .required {
            color: red;
        }
        
        textarea {
            resize: vertical;
            height: 80px;
        }
        
        .form-row {
            display: flex;
            gap: 10px;
        }
        
        .form-row .form-group {
            flex: 1;
        }
    </style>
</head>
<body>
    <div class="register-container">
        <h2>회원가입</h2>
        
        <form id="userForm" action="/register" method="post" onsubmit="return validateForm()">
            <div class="form-group">
                <label for="userId">아이디 <span class="required">*</span></label>
                <input type="text" id="userId" name="userId" required maxlength="20" 
                       placeholder="영문, 숫자 조합 4-20자">
                <button type="button" class="cancel-btn" onclick="chkId()">아이디 중복체크</button>
            </div>
            
            <div class="form-group">
                <label for="userPw">비밀번호 <span class="required">*</span></label>
                <input type="password" id="userPw" name="userPw" required minlength="8" 
                       placeholder="8자 이상 입력하세요">
            </div>
            
            <div class="form-group">
                <label for="confirmPw">비밀번호 확인 <span class="required">*</span></label>
                <input type="password" id="confirmPw" name="confirmPw" required minlength="8" 
                       placeholder="비밀번호를 다시 입력하세요">
            </div>
            
            <div class="form-group">
                <label for="userNm">이름 <span class="required">*</span></label>
                <input type="text" id="userNm" name="userNm" required maxlength="10" 
                       placeholder="실명을 입력하세요">
            </div>
            
            <div class="form-group">
                <label for="email">이메일 <span class="required">*</span></label>
                <input type="email" id="email" name="email" required 
                       placeholder="example@email.com">
            </div>
            
            <div class="form-group">
                <label for="phoneNum">전화번호 <span class="required">*</span></label>
                <input type="tel" id="phoneNum" name="phoneNum" required 
                       placeholder="010-1234-5678" pattern="[0-9]{3}-[0-9]{4}-[0-9]{4}">
            </div>
            
            <div class="form-group">
                <label>성별 <span class="required">*</span></label>
                <div class="gender-group">
                    <div class="gender-option">
                        <input type="radio" id="male" name="gender" value="M" required>
                        <label for="male">남성</label>
                    </div>
                    <div class="gender-option">
                        <input type="radio" id="female" name="gender" value="F" required>
                        <label for="female">여성</label>
                    </div>
                </div>
            </div>
            
            <div class="form-group">
                <div class="checkbox-group">
                    <input type="checkbox" id="personalInfoAcq" name="personalInfoAcq" value="Y" required>
                    <label for="personalInfoAcq">개인정보 수집 및 이용에 동의합니다 <span class="required">*</span></label>
                </div>
            </div>

            <div class="form-group">
                <div class="checkbox-group">
                    <input type="checkbox" id="InfoAcq" name="InfoAcq" value="Y" required>
                    <label for="InfoAcq">정보 수집 및 이용에 동의합니다 <span class="required">*</span></label>
                </div>
            </div>
            
            <button type="button" class="submit-btn" onclick="register()">회원가입</button>
            <button type="button" class="cancel-btn" onclick="goBack()">취소</button>
        </form>
    </div>

    <script>
        var chkIdStatus = 'N';

        function validateForm() {
            const password = document.getElementById('userPw').value;
            const confirmPassword = document.getElementById('confirmPw').value;
            
            if (password !== confirmPassword) {
                alert('비밀번호가 일치하지 않습니다.');
                return false;
            }
            
            const userId = document.getElementById('userId').value;
            const userIdRegex = /^[a-zA-Z0-9]{4,20}$/;
            if (!userIdRegex.test(userId)) {
                alert('아이디는 영문, 숫자 조합 4-20자로 입력해주세요.');
                return false;
            }
            
            const phoneNum = document.getElementById('phoneNum').value;
            const phoneRegex = /^[0-9]{3}-[0-9]{4}-[0-9]{4}$/;
            if (!phoneRegex.test(phoneNum)) {
                alert('전화번호는 010-1234-5678 형식으로 입력해주세요.');
                return false;
            }
            
            return true;
        }
        
        function goBack() {
            if (confirm('회원가입을 취소하시겠습니까?')) {
                window.history.back();
            }
        }
        
        // 전화번호 자동 하이픈 추가
        document.getElementById('phoneNum').addEventListener('input', function(e) {
            let value = e.target.value.replace(/[^0-9]/g, '');
            if (value.length >= 3 && value.length <= 7) {
                value = value.slice(0, 3) + '-' + value.slice(3);
            } else if (value.length > 7) {
                value = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7, 11);
            }
            e.target.value = value;
        });

        function register(){
            var formArray = $('#userForm').serializeArray();
    
            // 객체로 변환
            var formData = {};
            $.each(formArray, function(i, field) {
                formData[field.name] = field.value;
            });
            
            // 체크박스 처리 (필요시)
            formData.personalInfoAcq = $('#personalInfoAcq').is(':checked') ? 'Y' : 'N';

            // 체크박스 처리 (필요시)
            formData.infoAcq = $('#infoAcq').is(':checked') ? 'Y' : 'N';
            
            console.log('전송 데이터:', formData);

            if(chkIdStatus == "Y"){

                $.ajax({
                    url: '/register/userRegister',
                    type: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify(formData), // 수정된 부분
                    success: function(response) {
                        if (confirm("회원가입에 성공하였습니다. 로그인 페이지로 이동하시겠습니까?")) {
                            window.location.href = '/login';
                        }
                    },
                    error: function(xhr, status, error) {
                        console.error('에러:', error);
                        alert('서버 오류가 발생했습니다.');
                    }
                });
            }else{
                alert("아이디 중복체크를 진행하세요.");
            }
        }

        function chkId(){
            var userId = $("#userId").val();

            console.log("userId : ",userId);

            $.ajax({
                url: '/register/chkId',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({userId: userId}), // 수정된 부분
                success: function(response) {
                    if (response.status == "success") {
                        alert(response.message);
                        chkIdStatus = "Y";
                    } else {
                        alert(response.message);
                        chkIdStatus = "N";
                    }
                },
                error: function(xhr, status, error) {
                    console.error('에러:', error);
                    alert('서버 오류가 발생했습니다.');
                }
            });
        }
    </script>
</body>
</html>