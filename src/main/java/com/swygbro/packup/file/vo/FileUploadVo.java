package com.swygbro.packup.file.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;
import com.swygbro.packup.common.vo.CommonVo;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadVo extends CommonVo{
    private int objNo;
    private int cateNo;
    private String orgFileName;
    private String saveFileName;
    private String filePath;
    private long fileSize;
}