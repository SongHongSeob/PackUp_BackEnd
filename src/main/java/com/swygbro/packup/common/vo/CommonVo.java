package com.swygbro.packup.common.vo;

import java.util.Date;

import lombok.Data;

@Data
public class CommonVo {

    private String regId;
    private Date regDt;
    private String updId;
    private Date updDt;

    private int page;
    private int pageSize;
    private int offset;
    private String delYn;

    private String userId;

    private int sort;
}
