package com.adbest.smsmarketingfront.service.param;

import com.adbest.smsmarketingentity.MessageTemplate;
import com.adbest.smsmarketingentity.QMessageTemplate;
import com.adbest.smsmarketingfront.service.MessageTemplateService;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.QueryDslTools;
import com.querydsl.core.BooleanBuilder;
import lombok.Data;

/**
 * @see MessageTemplate
 * @see MessageTemplateService#findByConditions(GetMsgTemplatePage)
 */
@Data
public class GetMsgTemplatePage extends PageBase {
    
    private Boolean sms;  // 是否短信模板(true:是)
    private Boolean disable;  // 是否禁用(true:是)
    
    public void fillConditions(BooleanBuilder builder, QMessageTemplate qTemplate) {
        builder.and(qTemplate.customerId.eq(Current.get().getId()));
        QueryDslTools dslTools = new QueryDslTools(builder);
        dslTools.eqNotNull(qTemplate.sms, this.sms);
        dslTools.eqNotNull(qTemplate.disable, this.disable);
    }
}
