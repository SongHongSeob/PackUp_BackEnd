package com.swygbro.packup.notification.mapper;

import com.swygbro.packup.notification.vo.UserTemplateNoticeVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserTemplateNoticeMapper {

    /**
     * 현재 시간과 일치하는 알림 대상 조회
     */
    List<UserTemplateNoticeVo> selectNoticeTargetList(Map<String, String> param);
}