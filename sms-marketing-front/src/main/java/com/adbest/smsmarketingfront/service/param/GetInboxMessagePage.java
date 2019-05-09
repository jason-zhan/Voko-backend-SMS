package com.adbest.smsmarketingfront.service.param;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.MessageStatus;
import com.adbest.smsmarketingentity.QContacts;
import com.adbest.smsmarketingentity.QMessageRecord;
import com.adbest.smsmarketingfront.service.MessageRecordService;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.QueryDslTools;
import com.querydsl.core.BooleanBuilder;

/**
 * @see MessageRecord
 * @see MessageRecordService#findInboxByConditions(GetInboxMessagePage)
 */
public class GetInboxMessagePage extends PageBase {
    
    private Boolean hasRead;  // 是否已读(true:是)
    private Boolean sms;  // 是否短信(true:是)
    private String keyword;  // 关键词(联系人名字/姓氏/号码)
    
    public void fillConditions(BooleanBuilder builder, QMessageRecord qMessageRecord, QContacts qContacts) {
        QueryDslTools dslTools = new QueryDslTools(builder);
        dslTools.ifTrue(hasRead, qMessageRecord.status, MessageStatus.ALREADY_READ.getValue(), MessageStatus.UNREAD.getValue());
        dslTools.containsNotEmpty(false, this.keyword, qContacts.firstName, qContacts.lastName, qContacts.phone);
    }
}
