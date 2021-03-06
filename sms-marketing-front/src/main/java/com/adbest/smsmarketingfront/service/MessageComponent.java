package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.FinanceBill;
import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.MmsBill;
import com.adbest.smsmarketingentity.SmsBill;
import com.adbest.smsmarketingfront.entity.middleware.MsgPlanState;

/**
 * 消息(SMS/MMS)处理组件
 *
 * @see MessageRecord
 * @see SmsBill
 * @see MmsBill
 * @see FinanceBill
 */
public interface MessageComponent {
    
    /**
     * 更新消息状态
     *
     * @param sid
     * @param status e.g: queued, sent, delivered ...
     * @return
     */
    int updateMessageStatus(String sid, String status);
    
    /**
     * 任务结算
     *
     * @param planState
     * @return 创建任务信用额度消费
     */
    void msgPlanSettlement(MsgPlanState planState);
    
    /**
     * plan执行前的最终校验
     *
     * @param planId
     * @return 可执行消息量
     */
    MessagePlan validBeforeExec(Long planId);
    
    /**
     * 自动回复消息结算
     * @param message
     * @param remark  备注/说明
     */
    void autoReplySettlement(MessageRecord message, String remark);
    
    /**
     * 自动回复消息失败返还
     * @param message
     * @param remark  备注/说明
     */
    void autoReplyReturn(MessageRecord message, String remark);
    
    /**
     * 消息发送任务-发送消息失败的处理
     * 所有消息状态已达到最终状态，对于发送失败的消息，返还对应套餐余量或信用额度
     * [refer] https://www.twilio.com/docs/sms/api/message-resource#message-status-values
     *
     * @param plan
     */
    void validAndFinishPlan(MessagePlan plan);
}
