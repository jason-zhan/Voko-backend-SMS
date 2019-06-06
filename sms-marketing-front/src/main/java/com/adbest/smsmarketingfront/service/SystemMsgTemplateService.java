package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.SystemMsgTemplate;
import com.adbest.smsmarketingfront.service.param.GetSystemMsgTemplatePage;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * 系统模板前端业务
 *
 * @see SystemMsgTemplate
 */
public interface SystemMsgTemplateService {
    
    // 查看详情
    SystemMsgTemplate findById(Long id);
    
    // 根据条件查询
    Page<SystemMsgTemplate> findByConditions(GetSystemMsgTemplatePage getSysTemplatePage);
    
    // 模板类型列表
    Map<Integer, String> typeMap();
}
