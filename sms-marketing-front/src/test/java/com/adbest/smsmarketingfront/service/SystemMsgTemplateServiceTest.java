package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.SystemMsgTemplate;
import com.adbest.smsmarketingfront.service.param.GetSystemMsgTemplatePage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SystemMsgTemplateServiceTest {
    
    @Autowired
    private SystemMsgTemplateService systemMsgTemplateService;
    
    @Test
    public void findByConditions() {
        GetSystemMsgTemplatePage getSysTempPage = new GetSystemMsgTemplatePage();
        getSysTempPage.setType(2);
        Page<SystemMsgTemplate> templatePage = systemMsgTemplateService.findByConditions(getSysTempPage);
        System.out.println(templatePage.getTotalElements());
    }
}