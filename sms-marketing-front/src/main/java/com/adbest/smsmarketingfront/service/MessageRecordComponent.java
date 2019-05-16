package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.util.twilio.param.StatusCallbackParam;

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
    int updateMessageStatus(StatusCallbackParam param);
    
    // 接收消息处理
    MessageRecord dealWithIncome(MessageRecord income);
}
