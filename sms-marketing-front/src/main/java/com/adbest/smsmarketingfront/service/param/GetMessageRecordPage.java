package com.adbest.smsmarketingfront.service.param;

import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.service.MessageRecordService;
import com.adbest.smsmarketingfront.util.PageBase;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @see MessageRecord
 * @see MessageRecordService#findByConditions(GetMessageRecordPage)
 */
@Data
public class GetMessageRecordPage extends PageBase {
    
    private Boolean inbox; // 是否收件（true:是）
    /**
     * 计划id
     * @see MessagePlan#id
     */
    private Long planId;
    /**
     * 用户id
     * @see Customer#id
     */
    private Long customerId;
    private String contactsNumber;  // 联系人号码
    private Timestamp start;  // 创建时间开始
    private Timestamp end;  // 创建时间截止
    private String keyword;  // 关键词(短信内容)
}
