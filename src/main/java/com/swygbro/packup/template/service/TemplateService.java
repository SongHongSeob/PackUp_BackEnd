package com.swygbro.packup.template.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.swygbro.packup.file.service.FileService;
import com.swygbro.packup.file.vo.AttachFileVo;
import com.swygbro.packup.template.mapper.TemplateMapper;
import com.swygbro.packup.template.vo.CateObjVo;
import com.swygbro.packup.template.vo.TempStepObjVo;
import com.swygbro.packup.template.vo.TempStepTextVo;
import com.swygbro.packup.template.vo.TempStepVo;
import com.swygbro.packup.template.vo.TemplateVo;


@Service
@Transactional
public class TemplateService {

    @Autowired
    private TemplateMapper templateMapper;

    @Autowired
    private FileService fileService;

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> templateSave(TemplateVo tempVo, @RequestParam("imgFile") MultipartFile imgFile) throws IOException {

        Boolean saveStatus = true;

        Map<String,Object> responseMap = new HashMap<>();

        int templateSave = templateMapper.templateSave(tempVo);

        int newTemplateNo = tempVo.getTemplateNo();

        String userId = tempVo.getUserId();

        AttachFileVo fileVo = new AttachFileVo();

        fileVo.setFile(imgFile);
        fileVo.setRefNo(newTemplateNo);
        fileVo.setDelYn("N");
        fileVo.setUseYn("Y");
        fileVo.setFileCate1("template");
        fileVo.setFileCate2("thumnail");
        fileVo.setUserId(tempVo.getUserId());

        fileService.insertFile(fileVo);

        if(templateSave < 1){
            saveStatus = false;
            responseMap.put("status", saveStatus);
            responseMap.put("resposeText", "템플릿 저장시 오류 발생");
            return responseMap;
        }

        for(int i=0;i<tempVo.getStepsList().size();i++){
            tempVo.getStepsList().get(i).setTemplateNo(newTemplateNo);
            tempVo.getStepsList().get(i).setRegId(userId);
            tempVo.getStepsList().get(i).setUpdId(userId);
            int templateSaveStep = templateMapper.templateSaveStep(tempVo.getStepsList().get(i));

            if(templateSaveStep < 1){
                saveStatus = false;
                responseMap.put("status", saveStatus);
                responseMap.put("resposeText", "템플릿 스텝 저장시 오류 발생");
                return responseMap;
            }
            
            if(tempVo.getStepsList().get(i).getStepObjList().size() > 0){

                for(int t=0;t<tempVo.getStepsList().get(i).getStepObjList().size();t++){

                    tempVo.getStepsList().get(i).getStepObjList().get(t).setTemplateNo(newTemplateNo);
                    tempVo.getStepsList().get(i).getStepObjList().get(t).setStep(tempVo.getStepsList().get(i).getStep());
                    tempVo.getStepsList().get(i).getStepObjList().get(t).setTemplateStepNo(tempVo.getStepsList().get(i).getTemplateStepNo());
                    tempVo.getStepsList().get(i).getStepObjList().get(t).setRegId(userId);
                    tempVo.getStepsList().get(i).getStepObjList().get(t).setUpdId(userId);

                    int templateSaveStepObj = templateMapper.templateSaveStepObj(tempVo.getStepsList().get(i).getStepObjList().get(t));

                    if(templateSaveStepObj < 1){
                        saveStatus = false;
                        responseMap.put("status", saveStatus);
                        responseMap.put("resposeText", "템플릿 스텝 오브젝트 저장시 오류 발생");
                        return responseMap;
                    }
                }

                
            }

            
            
            if(tempVo.getStepsList().get(i).getStepTextList().size() > 0){

                for(int t=0;t<tempVo.getStepsList().get(i).getStepTextList().size();t++){

                    tempVo.getStepsList().get(i).getStepTextList().get(t).setTemplateNo(newTemplateNo);
                    tempVo.getStepsList().get(i).getStepTextList().get(t).setStep(tempVo.getStepsList().get(i).getStep());
                    tempVo.getStepsList().get(i).getStepTextList().get(t).setTemplateStepNo(tempVo.getStepsList().get(i).getTemplateStepNo());
                    tempVo.getStepsList().get(i).getStepTextList().get(t).setRegId(userId);
                    tempVo.getStepsList().get(i).getStepTextList().get(t).setUpdId(userId);

                    int templateSaveStepText = templateMapper.templateSaveStepText(tempVo.getStepsList().get(i).getStepTextList().get(t));

                    if(templateSaveStepText < 1){
                        saveStatus = false;
                        responseMap.put("status", saveStatus);
                        responseMap.put("resposeText", "템플릿 스텝 텍스트 저장시 오류 발생");
                        return responseMap;
                    }
                }

                
            }
        }

        responseMap.put("status", saveStatus);
        responseMap.put("resposeText", "템플릿 정상 저장");

        System.out.println("responseMap : "+responseMap);

        return responseMap;
    }

    public List<CateObjVo> getCateTemplateObject(CateObjVo objVo) {
        
        List<CateObjVo> objList = templateMapper.getCateTemplateObject(objVo);

        return objList;
    }

    /**
     * 템플릿 전체 데이터 조회
     */
    public TemplateVo getDetailData(Integer templateNo) {
        
        // 1. 템플릿 기본 정보 조회
        TemplateVo templateVo = templateMapper.getTemplate(templateNo);
        
        if(templateVo == null) {
            return null;
        }
        
        // 2. 스텝 목록 조회
        List<TempStepVo> stepsList = templateMapper.getStepsByTemplateNo(templateNo);
        
        // 3. 각 스텝의 하위 데이터 조회
        for(TempStepVo step : stepsList) {
            Integer stepNo = step.getStep();
            
            System.out.println("stepNo : "+stepNo);
            
            // 스텝 객체 목록 조회
            List<TempStepObjVo> stepObjList = templateMapper.getStepObjByStepNo(stepNo,templateNo);
            step.setStepObjList(stepObjList);
            
            // 스텝 텍스트 목록 조회
            List<TempStepTextVo> stepTextList = templateMapper.getStepTextByStepNo(stepNo,templateNo);
            step.setStepTextList(stepTextList);
        }
        
        templateVo.setStepsList(stepsList);
        
        return templateVo;
    }
    
    /**
     * 사용자별 템플릿 목록 조회
     */
    public List<TemplateVo> getTemplatesByUserId(TemplateVo tempVo) {
        
        int page = tempVo.getPage();

        if(page > 0) {
            int pageSize = 8;  // 한 페이지당 8개
            int offset = (page - 1) * pageSize;  // 페이지별 시작 위치 계산
            

            tempVo.setPageSize(pageSize);
            tempVo.setOffset(offset);
        }
        
        List<TemplateVo> templateList = templateMapper.getTemplatesByUserId(tempVo);
        
        return templateList;
    }
    
    @Transactional(rollbackFor = Exception.class)
	public Map<String, Object> templateUpdate(TemplateVo tempVo, @RequestParam("imgFile") MultipartFile imgFile) {
    	Boolean saveStatus = true;

        Map<String,Object> responseMap = new HashMap<>();

        int templateSave = templateMapper.templateUpdate(tempVo);

        int newTemplateNo = tempVo.getTemplateNo();

        AttachFileVo fileVo = new AttachFileVo();

        String userId = tempVo.getUserId();

        fileVo.setFile(imgFile);
        fileVo.setRefNo(newTemplateNo);
        fileVo.setDelYn("N");
        fileVo.setUseYn("Y");
        fileVo.setFileCate1("template");
        fileVo.setFileCate2("thumnail");
        fileVo.setUserId(tempVo.getUserId());

        try {
            fileService.updateFile(fileVo);
        } catch (IOException e) {
            saveStatus = false;
            responseMap.put("status", saveStatus);
            responseMap.put("resposeText", "파일 업로드 중 오류 발생: " + e.getMessage());
            return responseMap;
        }

        if(templateSave < 1){
            saveStatus = false;
            responseMap.put("status", saveStatus);
            responseMap.put("resposeText", "템플릿 저장시 오류 발생");
            return responseMap;
        }
        
        templateMapper.deleteTempalteStepObj(tempVo.getTemplateNo());
        templateMapper.deleteTempalteStepText(tempVo.getTemplateNo());
        
        for(int i=0;i<tempVo.getStepsList().size();i++){
            tempVo.getStepsList().get(i).setTemplateNo(tempVo.getTemplateNo());
            tempVo.getStepsList().get(i).setUpdId(userId);
            
            if(tempVo.getStepsList().get(i).getStepObjList().size() > 0){

                for(int t=0;t<tempVo.getStepsList().get(i).getStepObjList().size();t++){

                    tempVo.getStepsList().get(i).getStepObjList().get(t).setTemplateNo(tempVo.getTemplateNo());
                    tempVo.getStepsList().get(i).getStepObjList().get(t).setStep(tempVo.getStepsList().get(i).getStep());
                    tempVo.getStepsList().get(i).getStepObjList().get(t).setUpdId(userId);
                    tempVo.getStepsList().get(i).getStepObjList().get(t).setRegId(userId);
                    
                    int templateSaveStepObj = templateMapper.templateSaveStepObj(tempVo.getStepsList().get(i).getStepObjList().get(t));
                }

                
            }

            
            
            if(tempVo.getStepsList().get(i).getStepTextList().size() > 0){

                for(int t=0;t<tempVo.getStepsList().get(i).getStepTextList().size();t++){

                    tempVo.getStepsList().get(i).getStepTextList().get(t).setTemplateNo(tempVo.getTemplateNo());
                    tempVo.getStepsList().get(i).getStepTextList().get(t).setStep(tempVo.getStepsList().get(i).getStep());
                    tempVo.getStepsList().get(i).getStepTextList().get(t).setUpdId(userId);
                    tempVo.getStepsList().get(i).getStepTextList().get(t).setRegId(userId);
                    
                    int templateSaveStepText = templateMapper.templateSaveStepText(tempVo.getStepsList().get(i).getStepTextList().get(t));
                }

                
            }
        }

        responseMap.put("status", saveStatus);
        responseMap.put("resposeText", "템플릿 정상 저장");

        System.out.println("responseMap : "+responseMap);

        return responseMap;
	}

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> templateDelete(TemplateVo tempVo) {

      Boolean saveStatus = true;

      Map<String,Object> responseMap = new HashMap<>();

      int templateNo = tempVo.getTemplateNo();

      int tempObjDeleteCnt = templateMapper.deleteTempalteStepObjInt(templateNo);
      if(tempObjDeleteCnt < 1){
        saveStatus = false;
        responseMap.put("status", saveStatus);
        responseMap.put("resposeText", "템플릿 스탭 오브젝트 삭제시 오류 발생");
        return responseMap;
      }

      int tempTextDeleteCnt = templateMapper.deleteTempalteStepTextInt(templateNo);

      int tempStepDeleteCnt = templateMapper.deleteStepTemplate(templateNo);

      int tempDeleteCnt = templateMapper.deleteTemplate(templateNo);

      responseMap.put("status", saveStatus);
          responseMap.put("resposeText", "템플릿 정상 삭제");

      return responseMap;
  }

    public Map<String, Integer> getTemplateCnt(TemplateVo tempVo) {
        
        Map<String, Integer> getTemplateCnt = new HashMap<>();

        getTemplateCnt.put("totalCnt", templateMapper.getTotalCnt(tempVo));
        getTemplateCnt.put("totalFavoriteCnt", templateMapper.getTotalFavoriteCnt(tempVo));
        tempVo.setCateNo(1);
        getTemplateCnt.put("totalOfficeCnt", templateMapper.getTotalCateCnt(tempVo));
        tempVo.setCateNo(2);
        getTemplateCnt.put("totalDailyCnt", templateMapper.getTotalCateCnt(tempVo));
        tempVo.setCateNo(3);
        getTemplateCnt.put("totalTripCnt", templateMapper.getTotalCateCnt(tempVo));

        return getTemplateCnt;
    }

    public Map<String, Object> templateStatusUpdate(TemplateVo tempVo) {
        Boolean saveStatus = true;

        Map<String,Object> responseMap = new HashMap<>();

        int templateSave = templateMapper.templateStatusUpdate(tempVo);

        if(templateSave < 1){
            saveStatus = false;
            responseMap.put("status", saveStatus);
            responseMap.put("resposeText", "템플릿 즐겨찾기 수정시 오류 발생");
            return responseMap;
        }else{
            responseMap.put("status", saveStatus);
            responseMap.put("resposeText", "템플릿 정상 저장");
            return responseMap;
        }
    }

    public Map<String, Object> templateAlarmSave(TemplateVo tempVo) {
        Map<String,Object> responseMap = new HashMap<>();
        Boolean saveStatus = true;

        int templateAlarmSave = templateMapper.templateUpdate(tempVo);

        if(templateAlarmSave < 1){
            saveStatus = false;
            responseMap.put("status", saveStatus);
            responseMap.put("resposeText", "템플릿 알람 등록 중 오류 발생");
            return responseMap;
        }else{
            responseMap.put("status", saveStatus);
            responseMap.put("resposeText", "템플릿 알람 등록 정상 저장");
            return responseMap;
        }
    }
}
