package com.swygbro.packup.file.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.swygbro.packup.file.mapper.FileMapper;
import com.swygbro.packup.file.util.FileUploadUtil;
import com.swygbro.packup.file.vo.AttachFileVo;
import com.swygbro.packup.file.vo.FileUploadVo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class FileService {

    @Autowired
    private FileMapper fileMapper;

    public AttachFileVo getFilesByRefNo(int refNo,String fileCate1,String fileCate2) {
        return fileMapper.selectFilesByRefNo(refNo,fileCate1,fileCate2);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFile(int fileNo, String filePath) {
        
        int updateResult = fileMapper.updateFileDelYn(fileNo);
        if (updateResult < 1) {
            return false;
        }
        
        boolean fileDeleted = FileUploadUtil.deleteFile(filePath);
        if (!fileDeleted) {
            log.warn("물리적 파일 삭제 실패: {}", filePath);
        }
        
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public AttachFileVo insertFile(AttachFileVo fileVo) throws IOException {
        
        System.out.println("fileVo : "+fileVo);

        // 기존 파일들 모두 삭제하고 새 파일 업로드
        AttachFileVo uploadResult = FileUploadUtil.uploadObjFileWithDirectoryCleanup(fileVo);
        
        uploadResult.setRegId(fileVo.getUserId());
        uploadResult.setUpdId(fileVo.getUserId());
        uploadResult.setDelYn(fileVo.getDelYn());
        uploadResult.setUseYn(fileVo.getUseYn());
        uploadResult.setFileCate1(fileVo.getFileCate1());
        uploadResult.setFileCate2(fileVo.getFileCate2());

        System.out.println("uploadResult : "+uploadResult);

        int insertResult = fileMapper.insertAttachFile(uploadResult);
        if (insertResult < 1) {
            throw new RuntimeException("파일 정보 저장 실패: " + uploadResult.getOrgFileName());
        }
        
        log.info("파일 업데이트 완료: {}", uploadResult.getOrgFileName());
        return uploadResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public AttachFileVo updateFile(AttachFileVo fileVo) throws IOException {
        
        System.out.println("fileVo : "+fileVo);

        // 기존 파일들 모두 삭제하고 새 파일 업로드
        AttachFileVo uploadResult = FileUploadUtil.uploadObjFileWithDirectoryCleanup(fileVo);
        
        uploadResult.setRefNo(fileVo.getRefNo());
        uploadResult.setRegId(fileVo.getUserId());
        uploadResult.setUpdId(fileVo.getUserId());
        uploadResult.setDelYn(fileVo.getDelYn());
        uploadResult.setUseYn(fileVo.getUseYn());
        uploadResult.setFileCate1(fileVo.getFileCate1());
        uploadResult.setFileCate2(fileVo.getFileCate2());

        System.out.println("uploadResult : "+uploadResult);

        int insertResult = fileMapper.updateAttachFile(uploadResult);
        if (insertResult < 1) {
            throw new RuntimeException("파일 정보 수정 실패: " + uploadResult.getOrgFileName());
        }
        
        log.info("파일 업데이트 완료: {}", uploadResult.getOrgFileName());
        return uploadResult;
    }
}