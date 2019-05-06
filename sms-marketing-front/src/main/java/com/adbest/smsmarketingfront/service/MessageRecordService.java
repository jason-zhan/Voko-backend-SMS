package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.service.param.GetMessageRecordPage;
import org.springframework.data.domain.Page;

public interface MessageRecordService {
    
    // 根据id查询消息
    MessageRecord findById(Integer id);
    
    // 根据条件查询
    Page<MessageRecord> findByConditions(GetMessageRecordPage getMessagePage);
    
}
