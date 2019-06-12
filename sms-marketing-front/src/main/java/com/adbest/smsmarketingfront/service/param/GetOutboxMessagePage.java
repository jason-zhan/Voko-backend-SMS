package com.adbest.smsmarketingfront.service.param;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingentity.QContacts;
import com.adbest.smsmarketingentity.QMessageRecord;
import com.adbest.smsmarketingfront.service.MessageRecordService;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.QueryDslTools;
import com.querydsl.core.BooleanBuilder;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @see MessageRecord
 * @see MessageRecordService#findOutboxByConditions(GetOutboxMessagePage)
 */
@Data
public class GetOutboxMessagePage extends PageBase {
    
    private Boolean hasSent;  // 是否已发送(true:是)
    /**
     * 消息状态
     *
     * @see OutboxStatus
     */
    private Integer status;
    private Boolean isSms;  // 是否短信(true:是)
    private Long contactsGroupId;  // 联系人分组id
    private Timestamp start;  // (预期)发送时间开始
    private Timestamp end;  // (预期)发送时间截止
    private String keyword;  // 关键词(联系人名字/姓氏/号码)
    
    public void fillConditions(BooleanBuilder builder, QMessageRecord qMessageRecord, QContacts qContacts) {
        QueryDslTools dslTools = new QueryDslTools(builder);
        dslTools.eqNotNull(qMessageRecord.sms, this.isSms);
        dslTools.eqNotNull(qMessageRecord.status, this.status);
//        if (this.hasSent != null) {
//            if (this.hasSent) {
//                dslTools.isNotNull(qMessageRecord.sendTime);
//                dslTools.betweenNotNull(qMessageRecord.sendTime, this.start, this.end);
//            } else {
//                dslTools.isNull(qMessageRecord.sendTime);
//                dslTools.betweenNotNull(qMessageRecord.expectedSendTime, this.start, this.end);
//            }
//        }
        dslTools.betweenNotNull(qMessageRecord.expectedSendTime, this.start, this.end);
        dslTools.eqNotNull(qMessageRecord.contactsGroupId, this.contactsGroupId);
        dslTools.containsNotEmpty(false, this.keyword, qContacts.firstName, qContacts.lastName, qMessageRecord.contactsNumber);
    }
    
    @Override
    public String toString() {
        return "GetOutboxMessagePage{" +
                "hasSent=" + hasSent +
                ", status=" + status +
                ", isSms=" + isSms +
                ", contactsGroupId=" + contactsGroupId +
                ", start=" + start +
                ", end=" + end +
                ", keyword='" + keyword +
                ", page=" + page +
                ", size=" + size +
                "}";
    }
}
