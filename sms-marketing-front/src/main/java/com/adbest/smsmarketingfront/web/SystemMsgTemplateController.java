package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingentity.SystemMsgTemplate;
import com.adbest.smsmarketingfront.service.SystemMsgTemplateService;
import com.adbest.smsmarketingfront.service.param.GetSystemMsgTemplatePage;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system-msg-template")
public class SystemMsgTemplateController {
    
    @Autowired
    SystemMsgTemplateService systemMsgTemplateService;
    
    @RequestMapping("/details")
    @ResponseBody
    public ReturnEntity findById(Long id) {
        SystemMsgTemplate template = systemMsgTemplateService.findById(id);
        return ReturnEntity.success(template);
    }
    
    @RequestMapping("/list")
    @ResponseBody
    public ReturnEntity findByConditions(@RequestBody GetSystemMsgTemplatePage getSysTemplatePage) {
        Page<SystemMsgTemplate> templatePage = systemMsgTemplateService.findByConditions(getSysTemplatePage);
        return ReturnEntity.success(templatePage);
    }
}
