package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MessageTemplate;
import com.adbest.smsmarketingfront.service.param.CreateMsgTemplate;
import com.adbest.smsmarketingfront.service.param.GetMsgTemplatePage;
import com.adbest.smsmarketingfront.service.param.UpdateMsgTemplate;
import org.springframework.data.domain.Page;

import java.util.Set;

/**
 * 消息模板业务
 * @see MessageTemplate
 */
public interface MessageTemplateService {
    
    // 新增
    int create(CreateMsgTemplate create);
    
    // 更新
    int update(UpdateMsgTemplate update);
    
    // 启用/禁用
    int disableById(Long id, boolean disable);
    
    // 删除
    int deleteById(Long id);
    
    // 根据id查询
    MessageTemplate findById(Long id);
    
    // 根据条件查询
    Page<MessageTemplate> findByConditions(GetMsgTemplatePage getTemplatePage);
    
    // 模板变量
    Set<String> variableSet();
}
