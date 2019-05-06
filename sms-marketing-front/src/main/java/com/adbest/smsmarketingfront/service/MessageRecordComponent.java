package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MessageRecord;

/**
 * 消息(SMS/MMS)处理组件
 * @see MessageRecord
 */
public interface MessageRecordComponent {
    
    // 新增消息
    int createMessage(MessageRecord create);
    
    // 更新消息
    int updateMessage(MessageRecord update);
    
    // 更新消息状态
    int updateMessageStatus(int status);
    
    // 接收消息处理
    MessageRecord dealWithIncome(MessageRecord income);
}
