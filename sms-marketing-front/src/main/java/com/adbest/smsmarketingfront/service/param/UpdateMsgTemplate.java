package com.adbest.smsmarketingfront.service.param;

import com.adbest.smsmarketingentity.MessageTemplate;
import com.adbest.smsmarketingfront.service.MessageTemplateService;
import lombok.Data;

/**
 * @see MessageTemplate
 * @see MessageTemplateService#update(UpdateMsgTemplate)
 */
@Data
public class UpdateMsgTemplate extends CreateMsgTemplate {
    
    private Long id;  // 消息模板id
}
