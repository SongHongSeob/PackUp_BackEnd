package com.swygbro.packup.file.vo;

import org.springframework.web.multipart.MultipartFile;

import com.swygbro.packup.common.vo.CommonVo;

import lombok.Data;

@Data
public class AttachFileVo extends CommonVo{
    private int fileNo;
    private String fileCate1;                  // 파일 카테고리1(템플릿, 오브젝트)
    private String fileCate2;                  // 파일 카테고리2(썸네일, 일반 이미지)
    private int refNo;
    private String orgFileName;
    private String saveFileName;
    private String filePath;
    private long fileSize;
    private String useYn;
    private String delYn;

    private MultipartFile file;
}
