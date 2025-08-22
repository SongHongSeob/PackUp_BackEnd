package com.swygbro.packup.file.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.swygbro.packup.file.vo.AttachFileVo;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FileUploadUtil {

    private static String uploadPath;
    private static long maxFileSize;
    private static String allowedExtensions;

    @Value("${file.upload.path}")
    public void setUploadPath(String uploadPath) {
        FileUploadUtil.uploadPath = uploadPath;
    }

    @Value("${file.upload.max-size}")
    public void setMaxFileSize(long maxFileSize) {
        FileUploadUtil.maxFileSize = maxFileSize;
    }

    @Value("${file.upload.allowed-extensions}")
    public void setAllowedExtensions(String allowedExtensions) {
        FileUploadUtil.allowedExtensions = allowedExtensions;
    }

    public static boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("파일 삭제 완료: {}", filePath);
            } else {
                log.warn("파일이 존재하지 않음: {}", filePath);
            }
            return deleted;
        } catch (IOException e) {
            log.error("파일 삭제 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    public static boolean deleteAllFilesInDirectory(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (!Files.exists(path) || !Files.isDirectory(path)) {
                log.warn("디렉토리가 존재하지 않음: {}", directoryPath);
                return false;
            }
            
            Files.list(path).forEach(file -> {
                try {
                    if (Files.isRegularFile(file)) {
                        Files.delete(file);
                        log.info("파일 삭제 완료: {}", file.toString());
                    }
                } catch (IOException e) {
                    log.error("파일 삭제 실패: {}, 오류: {}", file.toString(), e.getMessage());
                }
            });
            
            log.info("디렉토리 내 모든 파일 삭제 완료: {}", directoryPath);
            return true;
        } catch (IOException e) {
            log.error("디렉토리 파일 삭제 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    public static boolean deleteSpecificFilesInDirectory(String directoryPath, List<String> fileNames) {
        try {
            Path path = Paths.get(directoryPath);
            if (!Files.exists(path) || !Files.isDirectory(path)) {
                log.warn("디렉토리가 존재하지 않음: {}", directoryPath);
                return false;
            }
            
            for (String fileName : fileNames) {
                Path filePath = path.resolve(fileName);
                if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                    Files.delete(filePath);
                    log.info("파일 삭제 완료: {}", filePath.toString());
                }
            }
            
            log.info("지정된 파일들 삭제 완료: {}", fileNames);
            return true;
        } catch (IOException e) {
            log.error("파일 삭제 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    public static AttachFileVo uploadObjFileWithDirectoryCleanup(AttachFileVo fileVo) throws IOException {
        validateFile(fileVo.getFile());
        
        // 1. 카테고리 디렉토리 경로 생성
        String categoryPath = createCategoryDirectory(fileVo);
        System.out.println("categoryPath : "+categoryPath);
        
        // 2. 기존 파일들 모두 삭제
        // deleteAllFilesInDirectory(categoryPath);
        
        // 3. 새 파일 업로드
        String savedFileName = generateUniqueFileName(fileVo.getFile().getOriginalFilename());
        String fullPath = categoryPath + "/" + savedFileName;
        
        Path destinationPath = Paths.get(fullPath);
        Files.copy(fileVo.getFile().getInputStream(), destinationPath);
        
        log.info("파일 업로드 완료 (기존 파일 삭제 후): {}", fullPath);
        
        return createFileUploadVo(fileVo.getFile(), savedFileName, fullPath, fileVo.getRefNo());
    }

    public static AttachFileVo updateObjFileWithDirectoryCleanup(AttachFileVo fileVo) throws IOException {
        validateFile(fileVo.getFile());
        
        // 1. 카테고리 디렉토리 경로 생성
        String categoryPath = createCategoryDirectory(fileVo);
        System.out.println("categoryPath : "+categoryPath);
        
        // 2. 기존 파일들 모두 삭제
        // deleteAllFilesInDirectory(categoryPath);
        
        // 3. 새 파일 업로드
        String savedFileName = generateUniqueFileName(fileVo.getFile().getOriginalFilename());
        String fullPath = categoryPath + "/" + savedFileName;
        
        Path destinationPath = Paths.get(fullPath);
        Files.copy(fileVo.getFile().getInputStream(), destinationPath);
        
        log.info("파일 업로드 완료 (기존 파일 삭제 후): {}", fullPath);
        
        return createFileUploadVo(fileVo.getFile(), savedFileName, fullPath, fileVo.getRefNo());
    }

    public static boolean isValidFile(MultipartFile file) {
        try {
            validateFile(file);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("파일 크기가 제한을 초과했습니다. 최대 크기: " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String fileExtension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        List<String> allowedExtList = Arrays.asList(allowedExtensions.split(","));
        
        if (!allowedExtList.contains(fileExtension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. 허용된 형식: " + allowedExtensions);
        }

        log.info("파일 타입: {}, 크기: {} bytes", file.getContentType(), file.getSize());
    }

    private static String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String nameWithoutExtension = getFileNameWithoutExtension(originalFileName);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return String.format("%s_%s_%s.%s", nameWithoutExtension, timestamp, uuid, extension);
    }

    private static String createCategoryDirectory(AttachFileVo fileVo) throws IOException {
        String categoryPath = "";
        if(fileVo.getFileCate1().equals("object")){
            categoryPath = uploadPath + "object";
        }else if(fileVo.getFileCate1().equals("template")){
            categoryPath = uploadPath + "template" + "/" + fileVo.getUserId() + "/thumnail/"+fileVo.getRefNo();
        }
        Path path = Paths.get(categoryPath);
        
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            log.info("디렉토리 생성: {}", categoryPath);
        }
        
        return categoryPath;
    }

    private static AttachFileVo createFileUploadVo(MultipartFile file, String savedFileName, String fullPath, int refNo) {
        AttachFileVo vo = new AttachFileVo();
        vo.setRefNo(refNo);
        vo.setOrgFileName(file.getOriginalFilename());
        vo.setSaveFileName(savedFileName);
        vo.setFilePath(fullPath);
        vo.setFileSize(file.getSize());
        vo.setRegDt(new Date());
        vo.setUpdDt(new Date());
        
        return vo;
    }

    private static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1) : "";
    }

    private static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
    }
}