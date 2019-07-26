package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.CreditBill;

import java.math.BigDecimal;
import java.util.List;

/**
 * 信用账单业务组件
 *
 * @see CreditBill
 */
public interface CreditBillComponent {
    
    /**
     * 调整用户最大信用额度
     *
     * @param customerId 用户id
     * @param amount     调整金额(+/-)
     * @return
     */
    void adjustCustomerMaxCredit(Long customerId, BigDecimal amount);
    
    /**
     * 保存任务信用消费账单
     *
     * @param customerId 用户id
     * @param planId     消息发送任务id
     * @param amount     金额(+/-)
     * @param remark     备注/描述
     * @return
     */
    void savePlanConsume(Long customerId, Long planId, BigDecimal amount, String remark);

    boolean cancellationQuota(List<Long> customerIds, BigDecimal amount);

    /**
     * 保存购买关键字信用消费账单
     *
     * @param keywordId
     * @param amount    金额(<0)
     * @param remark
     * @return
     */
    void saveKeywordConsume(Long keywordId, BigDecimal amount, String remark);

    /**
     * 保存购买手机号码信用消费账单
     *
     * @param customerId
     * @param mobileNumberId
     * @param amount         金额(<0)
     * @param remark
     * @return
     */
    void saveCustomerMobileConsume(Long customerId, Long mobileNumberId, BigDecimal amount, String remark);

    /**
     * 恢复用户可用信用额度
     *
     * @param customerId
     * @param amount     金额(>0)
     * @param remark
     * @return
     */
    void resumeAvailableCredit(Long customerId, BigDecimal amount, String remark);

    void saveAll(List<CreditBill> cBills);

}
