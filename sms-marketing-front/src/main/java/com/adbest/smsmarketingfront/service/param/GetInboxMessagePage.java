package com.adbest.smsmarketingfront.service.param;

import com.adbest.smsmarketingentity.InboxStatus;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.QContacts;
import com.adbest.smsmarketingentity.QMessageRecord;
import com.adbest.smsmarketingfront.service.MessageRecordService;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.QueryDslTools;
import com.querydsl.core.BooleanBuilder;
import lombok.Data;

/**
 * @see MessageRecord
 * @see MessageRecordService#findInboxByConditions(GetInboxMessagePage)
 */
@Data
public class GetInboxMessagePage extends PageBase {
    
    private Boolean hasRead;  // 是否已读(true:是)
    private Boolean isSms;  // 是否短信(true:是)
    private String keyword;  // 关键词(联系人名字/姓氏/号码)
    
    public void fillConditions(BooleanBuilder builder, QMessageRecord qMessageRecord, QContacts qContacts) {
        builder.and(qMessageRecord.customerId.eq(Current.get().getId()));
        builder.and(qMessageRecord.disable.isFalse());
        builder.and(qMessageRecord.inbox.isTrue());
        QueryDslTools dslTools = new QueryDslTools(builder);
        dslTools.ifTrue(this.hasRead, qMessageRecord.status, InboxStatus.ALREADY_READ.getValue(), InboxStatus.UNREAD.getValue());
        dslTools.eqNotNull(qMessageRecord.sms, this.isSms);
        dslTools.containsNotEmpty(false, this.keyword, qContacts.firstName, qContacts.lastName, qMessageRecord.contactsNumber);
    }
    
    @Override
    public String toString() {
        return "GetInboxMessagePage{" +
                "hasRead=" + hasRead +
                ", isSms=" + isSms +
                ", keyword='" + keyword +
                ", page=" + page +
                ", size=" + size +
                "}";
    }
}
