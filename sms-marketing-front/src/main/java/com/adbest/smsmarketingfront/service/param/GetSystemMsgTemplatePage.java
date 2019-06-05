package com.adbest.smsmarketingfront.service.param;

import com.adbest.smsmarketingentity.QSystemMsgTemplate;
import com.adbest.smsmarketingentity.SystemMsgTemplate;
import com.adbest.smsmarketingentity.SystemMsgTemplateType;
import com.adbest.smsmarketingfront.service.SystemMsgTemplateService;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.QueryDslTools;
import com.querydsl.core.BooleanBuilder;
import lombok.Data;

/**
 * @see SystemMsgTemplate
 * @see SystemMsgTemplateService#findByConditions(GetSystemMsgTemplatePage)
 */
@Data
public class GetSystemMsgTemplatePage extends PageBase {
    
    /**
     * 模板分类
     *
     * @see SystemMsgTemplateType
     */
    private Integer type;
    
    public void fillConditions(BooleanBuilder builder, QSystemMsgTemplate qSystemMsgTemplate) {
        builder.and(qSystemMsgTemplate.disable.isFalse());
        QueryDslTools.eqNotNull(builder, qSystemMsgTemplate.type, this.type);
    }
}
