package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingentity.MessageTemplate;
import com.adbest.smsmarketingfront.service.MessageTemplateService;
import com.adbest.smsmarketingfront.service.param.CreateMsgTemplate;
import com.adbest.smsmarketingfront.service.param.GetMsgTemplatePage;
import com.adbest.smsmarketingfront.service.param.UpdateMsgTemplate;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/message-template")
public class MessageTemplateController {
    
    @Autowired
    MessageTemplateService messageTemplateService;
    
    @RequestMapping("/create")
    @ResponseBody
    public ReturnEntity create(@RequestBody CreateMsgTemplate create) {
        int result = messageTemplateService.create(create);
        return ReturnEntity.success(result > 0);
    }
    
    @RequestMapping("/update")
    @ResponseBody
    public ReturnEntity update(@RequestBody UpdateMsgTemplate update) {
        int result = messageTemplateService.update(update);
        return ReturnEntity.success(result > 0);
    }
    
    @RequestMapping("/disable")
    @ResponseBody
    public ReturnEntity disableById(Long id, boolean disable) {
        int result = messageTemplateService.disableById(id, disable);
        return ReturnEntity.success(result > 0);
    }
    
    @RequestMapping("/delete")
    @ResponseBody
    public ReturnEntity deleteById(Long id) {
        int result = messageTemplateService.deleteById(id);
        return ReturnEntity.success(result > 0);
    }
    
    @RequestMapping("/details")
    @ResponseBody
    public ReturnEntity findById(Long id) {
        MessageTemplate template = messageTemplateService.findById(id);
        return ReturnEntity.success(template);
    }
    
    @RequestMapping("/page")
    @ResponseBody
    public ReturnEntity findByConditions(@RequestBody GetMsgTemplatePage getTemplatePage) {
        Page<MessageTemplate> templatePage = messageTemplateService.findByConditions(getTemplatePage);
        return ReturnEntity.success(templatePage);
    }
}
