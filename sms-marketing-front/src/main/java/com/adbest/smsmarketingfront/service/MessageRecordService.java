package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.service.param.GetMessageRecordPage;
import org.springframework.data.domain.Page;

/**
 * 消息(SMS/MMS)前端业务
 * @see MessageRecord
 */
public interface MessageRecordService {
    
    // 新增消息(草稿)
    int createMessage(MessageRecord create);
    
    // 修改消息(草稿)
    int updateMessage(MessageRecord update);
    
    // 根据id查询消息
    MessageRecord findById(Integer id);
    
    // 根据条件查询
    Page<MessageRecord> findByConditions(GetMessageRecordPage getMessagePage);
    
}
