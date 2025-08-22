package com.swygbro.packup.file.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.PathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.core.io.Resource;
import com.swygbro.packup.file.service.FileService;
import com.swygbro.packup.file.vo.AttachFileVo;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/files")
@Slf4j
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/insertFile")
    public ResponseEntity<AttachFileVo> updateFile(AttachFileVo fileVo) {
        
        try {
            AttachFileVo result = fileService.insertFile(fileVo);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("파일 업데이트 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/getImage")
    public ResponseEntity<Resource> getImage(@RequestParam int refNo, @RequestParam String fileCate1, @RequestParam String fileCate2) {

        // 1. DB에서 refNo로 파일 정보 조회
        AttachFileVo files = fileService.getFilesByRefNo(refNo,fileCate1,fileCate2);

        // 2. 첫 번째 파일의 실제 경로 가져옴
        Path filePath = Paths.get(files.getFilePath()); // 실제 파일 경로

        // 3. 파일을 브라우저로 전송
        Resource resource = new PathResource(filePath);

        return ResponseEntity.ok().body(resource);
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam int refNo, @RequestParam String fileCate1, @RequestParam String fileCate2) {
        try {
            // 1. DB에서 refNo로 파일 정보 조회
            AttachFileVo fileInfo = fileService.getFilesByRefNo(refNo, fileCate1, fileCate2);

            System.out.println("fileInfo : "+fileInfo);
            
            if (fileInfo == null) {
                return ResponseEntity.notFound().build();
            }

            // 2. 파일 경로 가져오기
            Path filePath = Paths.get(fileInfo.getFilePath());
            Resource resource = new PathResource(filePath);

            // 3. 파일이 존재하는지 확인
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // 4. 원본 파일명 URL 인코딩
            String encodedFileName = URLEncoder.encode(fileInfo.getOrgFileName(), "UTF-8")
                    .replaceAll("\\+", "%20");

            System.out.println("encodedFileName : "+encodedFileName);
            System.out.println("fileInfo.getSaveFileName() : "+fileInfo.getSaveFileName());

            // 5. 다운로드 응답 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + encodedFileName);
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (UnsupportedEncodingException e) {
            log.error("파일명 인코딩 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("파일 다운로드 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}