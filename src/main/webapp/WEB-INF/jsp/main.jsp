<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PackUp - 메인 페이지</title>
    <style>
        body {
            font-family: 'Arial', sans-serif;
            margin: 0;
            padding: 0;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }
        
        .header {
            background: rgba(255, 255, 255, 0.1);
            backdrop-filter: blur(10px);
            padding: 1rem 2rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .logo {
            color: white;
            font-size: 2rem;
            font-weight: bold;
        }
        
        .nav-links {
            display: flex;
            gap: 2rem;
        }
        
        .nav-links a {
            color: white;
            text-decoration: none;
            padding: 0.5rem 1rem;
            border-radius: 5px;
            transition: background-color 0.3s;
        }
        
        .nav-links a:hover {
            background: rgba(255, 255, 255, 0.2);
        }
        
        .container {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            text-align: center;
            padding: 2rem;
        }
        
        .welcome {
            color: white;
            margin-bottom: 2rem;
        }
        
        .welcome h1 {
            font-size: 3rem;
            margin-bottom: 1rem;
        }
        
        .welcome p {
            font-size: 1.2rem;
            margin-bottom: 2rem;
        }
        
        .action-buttons {
            display: flex;
            gap: 1rem;
            flex-wrap: wrap;
            justify-content: center;
        }
        
        .btn {
            padding: 1rem 2rem;
            font-size: 1rem;
            border: none;
            border-radius: 25px;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
            transition: all 0.3s;
        }
        
        .btn-primary {
            background: #4CAF50;
            color: white;
        }
        
        .btn-primary:hover {
            background: #45a049;
            transform: translateY(-2px);
        }
        
        .btn-secondary {
            background: rgba(255, 255, 255, 0.2);
            color: white;
            border: 2px solid white;
        }
        
        .btn-secondary:hover {
            background: white;
            color: #667eea;
            transform: translateY(-2px);
        }

        .notification-area {
            position: relative;
            font-size: 1.8rem;
            cursor: pointer;
            color: white;
        }

        .badge {
            position: absolute;
            top: -10px;
            right: -10px;
            background: red;
            color: white;
            padding: 4px 8px;
            border-radius: 50%;
            font-size: 0.7rem;
            animation: shake 0.4s ease-in-out;
        }

        .hidden {
            display: none;
        }

        @keyframes shake {
            0% { transform: rotate(0); }
            20% { transform: rotate(-10deg); }
            40% { transform: rotate(10deg); }
            60% { transform: rotate(-5deg); }
            80% { transform: rotate(5deg); }
            100% { transform: rotate(0); }
        }
    </style>
</head>
<body>
    <header class="header">
        <div class="logo">PackUp</div>
        <nav class="nav-links">
            <!-- 알림 리스트 영역 -->
            <div id="notificationList" style="display:none; position:absolute; top:60px; right:60px; background:white; border-radius:8px; box-shadow:0 0 10px rgba(0,0,0,0.2); max-width:300px; padding:10px; z-index:1000;"></div>
            <div class="notification-area">
                🔔<span id="badge" class="badge hidden">0</span>
            </div>
            <a href="/dashboard">대시보드</a>
            <a href="/mypage">마이페이지</a>
            <a href="/lgn/login">로그인</a>
        </nav>
    </header>
    
    <main class="container">
        <div class="welcome">
            <h1>PackUp에 오신 것을 환영합니다!</h1>
            <p>효율적인 패키징 관리 시스템으로 더 나은 비즈니스를 시작하세요.</p>
        </div>
        
        <div class="action-buttons">
            <a href="/dashboard" class="btn btn-primary">시작하기</a>
            <a href="/lgn/login" class="btn btn-secondary">로그인</a>
        </div>
    </main>

<script>
    const userId = "kei01105@naver.com";
    //const userId = "${sessionScope.loginUserId}"; // 실제 서비스에서는 세션 기반으로 치환
    const badge = document.getElementById("badge");
    const notificationListDiv = document.createElement("div");

    notificationListDiv.id = "notificationList";
    notificationListDiv.style.cssText = `
        display: none;
        position: absolute;
        top: 60px;
        right: 60px;
        background: white;
        border-radius: 8px;
        box-shadow: 0 0 10px rgba(0,0,0,0.2);
        max-width: 300px;
        padding: 10px;
        z-index: 1000;
        font-size: 0.9rem;
    `;
    document.body.appendChild(notificationListDiv);

    // --- 1. 안 읽은 알림 개수 조회 ---
    fetch("/notifications/unread_count?userId=" + userId)
        .then(res => res.json())
        .then(data => {
            if (data.count > 0) {
                badge.textContent = data.count;
                badge.classList.remove("hidden");
            }
        })
        .catch(err => console.error("뱃지 조회 실패:", err));

    // --- 2. 종모양 클릭: 읽음 처리 + 뱃지 숨김 + 알림 목록 표시 ---
    document.querySelector(".notification-area").addEventListener("click", () => {
        // 읽음 처리
        fetch("/notifications/readAll", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ userId: userId })
        }).then(res => {
            if (res.ok) {
                badge.textContent = "0";
                badge.classList.add("hidden");
            }
        });

        // 알림 리스트 불러오기
        fetch("/notifications/list?userId=" + userId + "&limit=10")
            .then(res => res.json())
            .then(data => {
                if (!data || data.length === 0) {
                    notificationListDiv.innerHTML = "<p style='padding:5px'>알림이 없습니다.</p>";
                } else {
                    const html = data.map(item => {
                        const color = item.readYn ? "#f0f0f0" : "#ffffff";
                        const time = item.notificationTime || item.sentAt || "";
                        return `
                            <div style="padding:10px; margin-bottom:5px; border-radius:5px; background:${color};">
                                <strong>${item.templateNm || "알림"}</strong><br/>
                                <span>${item.message}</span><br/>
                                <span style="color:gray; font-size:0.75rem;">${time}</span>
                            </div>
                        `;
                    }).join("");
                    notificationListDiv.innerHTML = html;
                }
                // 리스트 토글
                notificationListDiv.style.display = notificationListDiv.style.display === "none" ? "block" : "none";
            })
            .catch(err => console.error("알림 목록 불러오기 실패:", err));
    });

    // 바깥 클릭 시 알림 목록 숨기기
    document.addEventListener("click", (e) => {
        const notifArea = document.querySelector(".notification-area");
        if (!notifArea.contains(e.target) && !notificationListDiv.contains(e.target)) {
            notificationListDiv.style.display = "none";
        }
    });

    // --- 3. SSE 연결 ---
    const evtSource = new EventSource("/notifications/subscribe?userId=" + userId);

    evtSource.addEventListener("alarm", (event) => {
        console.log("알림 수신:", event.data);
        let current = parseInt(badge.textContent || "0", 10);
        badge.textContent = current + 1;
        badge.classList.remove("hidden");

        // 종모양 애니메이션 재실행
        badge.classList.remove("shake");
        void badge.offsetWidth;
        badge.classList.add("shake");
    });

    evtSource.onerror = (err) => {
        console.error("SSE 오류:", err);
    };
</script>

</body>
</html>