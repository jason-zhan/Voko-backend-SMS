package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.util.twilio.param.StatusCallbackParam;

/**
 * 消息(SMS/MMS)处理组件
 * @see MessageRecord
 * @see
 */
public interface MessageComponent {
    
    // 更新消息状态
    int updateMessageStatus(StatusCallbackParam param);
    
    /**
     * 消息结算
     * @param customerId 用户id
     * @param isSms 是否短信(true:是)
     * @param amount 消息数量
     */
    void messageSettlement(Long customerId, boolean isSms, int amount, String remark);
}
