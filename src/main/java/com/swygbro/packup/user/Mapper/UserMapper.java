package com.swygbro.packup.user.Mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.swygbro.packup.config.CustomUserDetails;
import com.swygbro.packup.user.vo.UserVo;

@Mapper
public interface UserMapper {

    int insertUser(UserVo userDto);

    CustomUserDetails selectUserById(@Param("userId") String userId);

    int updateLastLoginDate(@Param("userId") String userId);

    UserVo getUserInfo(@Param("userId") String userId);

    int updateUser(UserVo userVo);

    UserVo getUserByEmail(@Param("userId") String userId);

}
