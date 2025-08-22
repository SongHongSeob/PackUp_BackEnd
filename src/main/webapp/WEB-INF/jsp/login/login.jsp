<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>Login</title>
</head>
<body>
Login Page

<!-- 에러 메시지 표시 (로그인 실패 시) -->
<c:if test="${param.error != null}">
    <div style="color: red;">
        로그인 실패: 사용자명 또는 비밀번호를 확인하세요.
    </div>
</c:if>

<!-- 로그아웃 성공 메시지 -->
<c:if test="${param.logout != null}">
    <div style="color: green;">
        성공적으로 로그아웃되었습니다.
    </div>
</c:if>


<form action="/loginProcess" method="post" name="loginForm">
  <input id="username" type="text" name="username" placeholder="username" required />
  <input id="password" type="password" name="password" placeholder="password" required />
  <button type="submit" value="login">Submit</button>
  <button type="button" value="login" onclick="location.href='/login/register'">Register</button>
</form>

<a href="/oauth2/authorization/kakao">카카오 로그인</a>
<a href="/oauth2/authorization/naver">네이버 로그인</a>
<a href="/oauth2/authorization/google">구글 로그인</a>

</body>
</html>