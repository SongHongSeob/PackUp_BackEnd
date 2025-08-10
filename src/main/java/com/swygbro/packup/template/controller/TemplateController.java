package com.swygbro.packup.template.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.swygbro.packup.file.vo.AttachFileVo;
import com.swygbro.packup.template.service.TemplateService;
import com.swygbro.packup.template.vo.CateObjVo;
import com.swygbro.packup.template.vo.TemplateVo;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@RequestMapping("/temp")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @PostMapping("/getCateTemplateObject")
    public ResponseEntity<Map<String, Object>> getCateTemplateObject(@RequestBody(required = false) CateObjVo ObjVo) {
        Map<String, Object> response = new HashMap<>();
        
        try {
                        
            // 서비스 호출
            List<CateObjVo> teplateObj = templateService.getCateTemplateObject(ObjVo);
            
            // 결과 검증
            if (teplateObj.size() != 0) {
                // 성공
                response.put("objList", teplateObj);
                response.put("responseText", "success");
                response.put("message", "카테고리의 오브젝트 조회가 완료되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                // 데이터 없음
                response.put("objList", new ArrayList<>());
                response.put("responseText", "fail");
                response.put("message", "조회된 오브젝트가 없습니다.");
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            log.error("카테고리 템플릿 조회 중 오류 발생 - ObjVo: {}, error: {}", 
                    ObjVo, e.getMessage(), e);
            
            // 서버 오류
            response.put("objList", new ArrayList<>());
            response.put("responseText", "fail");
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/templateSave")
    public ResponseEntity<Map<String, Object>> templateSave(TemplateVo tempVo,
                                                                @RequestParam("imgFile") MultipartFile imgFile) throws IOException{

        Map<String, Object> teplateSaveMap = templateService.templateSave(tempVo,imgFile);

        Map<String, Object> response = new HashMap<>();

        if(Boolean.TRUE.equals(teplateSaveMap.get("status"))) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        }else{
            response.put("status", "fail");
            return ResponseEntity.badRequest().body(response);
        }

    }

    @PostMapping("/templateUpdate")
    public ResponseEntity<Map<String, Object>> templateUpdate(TemplateVo tempVo,
                                                                @RequestParam("imgFile") MultipartFile imgFile){

        Map<String, Object> teplateSaveMap = templateService.templateUpdate(tempVo, imgFile);

        Map<String, Object> response = new HashMap<>();

        if(Boolean.TRUE.equals(teplateSaveMap.get("status"))) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        }else{
            response.put("status", "fail");
            return ResponseEntity.badRequest().body(response);
        }

    }

    @PostMapping("/templateDelete")
    public ResponseEntity<Map<String, Object>> templateDelete(@RequestBody TemplateVo tempVo){

        Map<String, Object> teplateSaveMap = templateService.templateDelete(tempVo);

        Map<String, Object> response = new HashMap<>();

        if(Boolean.TRUE.equals(teplateSaveMap.get("status"))) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        }else{
            response.put("status", "fail");
            return ResponseEntity.badRequest().body(response);
        }

    }

    @PostMapping("/getDetailData")
    public ResponseEntity<Map<String, Object>> getDetailData(@RequestBody TemplateVo tempVo) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 입력 검증
            if (tempVo.getTemplateNo() <= 0) {
                response.put("templateData", null);
                response.put("responseText", "fail");
                response.put("message", "유효하지 않은 템플릿 번호입니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 서비스 호출
            tempVo = templateService.getDetailData(tempVo.getTemplateNo());
            
            // 조회 결과 검증
            if (tempVo == null) {
                response.put("templateData", null);
                response.put("responseText", "fail");
                response.put("message", "해당 템플릿을 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // 성공 응답
            response.put("templateData", tempVo);
            response.put("responseText", "success");
            response.put("message", "템플릿 조회가 완료되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("템플릿 상세 조회 중 오류 발생 - templateNo: {}, error: {}", 
                    tempVo.getTemplateNo(), e.getMessage(), e);
            
            response.put("templateData", null);
            response.put("responseText", "fail");
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/getUserTemplateDataList")
    public ResponseEntity<Map<String, Object>> getUserTemplateDataList(@RequestBody TemplateVo tempVo, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null || authentication.getName() == null) {
            response.put("success", false);
            response.put("message", "인증되지 않은 사용자입니다.");
            return ResponseEntity.status(401).body(response);
        }

        String userId = authentication.getName();
        log.info("Getting user tempateInfo for: {}", userId);

        tempVo.setUserId(userId);
        
        List<TemplateVo> userTempList = templateService.getTemplatesByUserId(tempVo);
        Map<String, Integer> templateCnt = templateService.getTemplateCnt(tempVo);

        response.put("templateDataList", userTempList);
        response.put("templateCntList", templateCnt);
        response.put("responseText", "success");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/templateStatusUpdate")
    public ResponseEntity<Map<String, Object>> templateStatusUpdate(@RequestBody TemplateVo tempVo,
                                                                Authentication authentication){
        
        tempVo.setUpdId(authentication.getName());
        Map<String, Object> teplateUpdateMap = templateService.templateStatusUpdate(tempVo);
                                                                    
        Map<String, Object> response = new HashMap<>();

        if(Boolean.TRUE.equals(teplateUpdateMap.get("status"))) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        }else{
            response.put("status", "fail");
            return ResponseEntity.badRequest().body(response);
        }

    }

}