package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.service.param.GetMessageRecordPage;
import org.springframework.data.domain.Page;

/**
 * 消息(SMS/MMS)前端业务
 * @see MessageRecord
 */
public interface MessageRecordService {
    
    // 根据id查询消息
    MessageRecord findById(Long id);
    
    // 根据条件查询收件
    Page<MessageRecord> findInboxByConditions(GetMessageRecordPage getMessagePage);
    
    // 根据条件查询发件
    Page<MessageRecord> findOutboxByConditions(GetMessageRecordPage getMessagePage);
    
}
