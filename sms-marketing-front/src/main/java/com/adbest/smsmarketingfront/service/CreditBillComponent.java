package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.CreditBill;

import java.math.BigDecimal;

/**
 * 信用账单业务组件
 * @see CreditBill
 */
public interface CreditBillComponent {
    
    /**
     * 调整用户最大信用额度
     * @param customerId 用户id
     * @param amount  调整金额(+/-)
     * @return
     */
    boolean adjustCustomerMaxCredit(Long customerId, BigDecimal amount);
    
    /**
     * 保存任务信用消费账单
     * @param customerId  用户id
     * @param planId  消息发送任务id
     * @param amount  金额
     * @param remark  备注/描述
     * @return
     */
    boolean savePlanConsume(Long customerId, Long planId, BigDecimal amount, String remark);
    
}
