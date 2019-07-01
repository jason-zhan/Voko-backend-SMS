package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.FinanceBill;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.MmsBill;
import com.adbest.smsmarketingentity.SmsBill;
import com.adbest.smsmarketingfront.util.twilio.param.StatusCallbackParam;

/**
 * 消息(SMS/MMS)处理组件
 * @see MessageRecord
 * @see SmsBill
 * @see MmsBill
 * @see FinanceBill
 */
public interface MessageComponent {
    
    // 更新消息状态
    int updateMessageStatus(StatusCallbackParam param);
    
    // 消息发送任务结算
    void messagePlanSettlement(Long customerId, Long planId, int amount, boolean isSms);
    
    /**
     * 自动回复消息结算
     * @param customerId
     * @param amount 消息数量
     * @param isSms 是否短信(true:是)
     */
    void autoReplySettlement(Long customerId, int amount, boolean isSms);
}
