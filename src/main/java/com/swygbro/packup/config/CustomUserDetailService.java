package com.swygbro.packup.config;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.swygbro.packup.user.Mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserMapper userMapper;
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        CustomUserDetails users = userMapper.selectUserById(userId);
        
        if(users == null){
            throw new UsernameNotFoundException("userId : "+userId+" 찾지 못함");
        }

        System.out.println("**************Found user***************");
        System.out.println("   userId : " + users.getUsername());
        System.out.println("   enabled : " + users.isEnabled());
        System.out.println("   role : " + users.getRole());
        System.out.println("***************************************");

        return users;
    }
}
