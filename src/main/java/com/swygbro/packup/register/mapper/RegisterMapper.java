package com.swygbro.packup.register.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.swygbro.packup.user.vo.UserVo;

@Mapper
public interface RegisterMapper {

    int chkId(String userId);

    int insertUser(UserVo userVo);

}
