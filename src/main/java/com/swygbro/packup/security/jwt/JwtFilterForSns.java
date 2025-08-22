package com.swygbro.packup.security.jwt;

import com.swygbro.packup.user.entity.User;
import com.swygbro.packup.user.vo.UserVo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Todo 로그인 한 사용자가 api 호출 시 유지하는 코드(아직 미완성 - 후에 필요시 작성하기)
@RequiredArgsConstructor
public class JwtFilterForSns extends OncePerRequestFilter {
    private final JwtUtill jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        String path = request.getRequestURI();

        // 헤더
        //String authorization = request.getHeader("Authorization");
        String authorization = null;

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            filterChain.doFilter(request, response);
            return;
        }

        for (Cookie cookie : cookies) {
            // System.out.println( cookie.getName() );
            if (cookie.getName().equals("authorization")) {
                authorization = cookie.getValue();
            }
        }

        // 헤더 확인
        if(authorization == null){
            // System.out.println("token null");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization;

        // 토큰 사리진지 확인
        if(jwtUtil.isExpired(token)){
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰에서 usernamer과 role 획득
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getrole(token);

        User user = new  User();
        user.setUserNm(username);
        user.setUserPw("temp");
        user.setRole(role);

        //ToDo 세션에 사용자 등록



    }
}
