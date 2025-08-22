package com.swygbro.packup.dashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.swygbro.packup.template.service.TemplateService;
import com.swygbro.packup.template.vo.TemplateVo;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dashboard")
@NoArgsConstructor
public class DashboardController {

    @Autowired
    private TemplateService templateService;

    @GetMapping("/dashboard")
    public ModelAndView dashboard(Authentication authentication) {
        // 인증 정보가 없거나 인증되지 않은 경우 main.jsp로 리다이렉트
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            log.info("Unauthorized access to dashboard, redirecting to main");
            return new ModelAndView("redirect:/");
        }

        String userId = authentication.getName();

        List<TemplateVo> templateList = new ArrayList<>();
        TemplateVo tempVo = new TemplateVo();
        tempVo.setUserId(userId);
        tempVo.setPage(0);

        templateList = templateService.getTemplatesByUserId(tempVo);
        
        log.info("Dashboard accessed by user: {}", authentication.getName());
        
        ModelAndView mv = new ModelAndView("dashboard/dashboard");
        mv.addObject("username", userId);
        mv.addObject("tempList", templateList);
        mv.addObject("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return mv;
    }

    @PostMapping("/getUserData")
    @ResponseBody
    public List<TemplateVo> getUserData(@ModelAttribute TemplateVo tempVo) {

        List<TemplateVo> templateList = new ArrayList<>();
        int page = tempVo.getPage();
        tempVo.setPage(page);

        templateList = templateService.getTemplatesByUserId(tempVo);
        
        return templateList;
    }
}