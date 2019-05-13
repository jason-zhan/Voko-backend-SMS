package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.service.param.GetInboxMessagePage;
import com.adbest.smsmarketingfront.service.param.GetOutboxMessagePage;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 消息(SMS/MMS)前端业务
 * @see MessageRecord
 */
public interface MessageRecordService {
    
    // 删除(实际禁用)一条消息
    int deleteOneMessage(Long id);
    
    // 批量删除(实际禁用)
    int batchDelete(List<Long> idList);
    
    // 根据id查询消息
    MessageRecord findById(Long id);
    
    // 根据条件查询收件
    Page<MessageRecord> findInboxByConditions(GetInboxMessagePage getInboxPage);
    
    // 根据条件查询发件
    Page<MessageRecord> findOutboxByConditions(GetOutboxMessagePage getOutboxPage);
    
}
