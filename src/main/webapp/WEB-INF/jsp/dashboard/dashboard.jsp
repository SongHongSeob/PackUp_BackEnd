<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>대시보드</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .header {
            background-color: #007bff;
            color: white;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
        }
        .welcome-message {
            font-size: 24px;
            margin-bottom: 10px;
        }
        .btn {
            border: none;
            padding: 10px 20px;
            border-radius: 4px;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
            margin-right: 10px;
        }
        .logout-btn {
            background-color: #dc3545;
            color: white;
        }
        .logout-btn:hover {
            background-color: #c82333;
        }
        .mypage-btn {
            background-color: #28a745;
            color: white;
        }
        .mypage-btn:hover {
            background-color: #218838;
        }
        #downloadResult.success {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        #downloadResult.error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
    </style>
</head>
<body>
    <div class="header">
        <div class="welcome-message">환영합니다, ${username}님!</div>
        <div>
            <a href="/mypage/mypage" class="btn mypage-btn">마이페이지</a>
            <form action="/logout" method="post" style="display: inline;">
                <button type="submit" class="btn logout-btn">로그아웃</button>
            </form>
        </div>
        <div>
            <img src="/files/getImage?refNo=6&fileCate1=template&fileCate2=thumnail" alt="이미지 미리보기" style="max-width: 200px; max-height: 200px;">
        </div>
    </div>

    <script>
        // 사용자 정보 체크
        <c:if test="${empty username}">
            window.location.href = '/';
        </c:if>
        
        console.log("imageRefNo : "+$('#refNo').val());
        console.log("fileCate1 : "+$('#fileCate1').val());
        console.log("fileCate2 : "+$('#fileCate2').val());
        
        // 파일 다운로드 함수
        function downloadFile() {
            const refNo = $('#refNo').val();
            const fileCate1 = $('#fileCate1').val();
            const fileCate2 = $('#fileCate2').val();
            
            if (!refNo || !fileCate1 || !fileCate2) {
                showResult('모든 필드를 입력해주세요.', 'error');
                return;
            }
            
            // 다운로드 URL 생성
            const downloadUrl = '/files/download?refNo=' + refNo + '&fileCate1=' + fileCate1 + '&fileCate2=' + fileCate2;

            console.log("downloadUrl : "+downloadUrl);
            
            // 파일 존재 여부 먼저 확인 (HEAD 요청)
            $.ajax({
                url: downloadUrl,
                type: 'HEAD',
                success: function() {
                    // 파일이 존재하면 다운로드 시작
                    showResult('파일 다운로드를 시작합니다...', 'success');
                    
                    // 새 창에서 다운로드 실행
                    const link = document.createElement('a');
                    link.href = downloadUrl;
                    link.download = '';
                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);
                },
                error: function(xhr) {
                    if (xhr.status === 404) {
                        showResult('파일을 찾을 수 없습니다.', 'error');
                    } else {
                        showResult('파일 다운로드 중 오류가 발생했습니다.', 'error');
                    }
                }
            });
        }
        
        // 결과 메시지 표시 함수
        function showResult(message, type) {
            const resultDiv = $('#downloadResult');
            resultDiv.removeClass('success error');
            resultDiv.addClass(type);
            resultDiv.text(message);
            resultDiv.show();
            
            // 3초 후 메시지 숨기기
            setTimeout(() => {
                resultDiv.hide();
            }, 3000);
        }
    </script>
    
    <div>
        <h2>대시보드</h2>
        <p>로그인에 성공하셨습니다.</p>
        <p>현재 시간: <c:out value="${currentTime}" /></p>
        
        <!-- 파일 다운로드 테스트 섹션 -->
        <div style="margin-top: 30px; padding: 20px; border: 1px solid #ddd; border-radius: 8px;">
            <h3>파일 다운로드 테스트</h3>
            <form style="margin-bottom: 15px;">
                <label>REF_NO: </label>
                <input type="number" id="refNo" value="6" style="margin-right: 10px;">
                
                <label>FILE_CATE1: </label>
                <input type="text" id="fileCate1" value="template" style="margin-right: 10px;">
                
                <label>FILE_CATE2: </label>
                <input type="text" id="fileCate2" value="thumnail" style="margin-right: 10px;">
                
                <button type="button" onclick="downloadFile()" class="btn" style="background-color: #17a2b8; color: white;">파일 다운로드</button>
            </form>
            
            <div id="downloadResult" style="margin-top: 10px; padding: 10px; display: none;"></div>
        </div>
    </div>
</body>
</html>