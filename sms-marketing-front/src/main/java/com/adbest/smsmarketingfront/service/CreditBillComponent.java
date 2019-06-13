package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.CreditBill;

import java.math.BigDecimal;

/**
 * 用户信用额度业务组件
 * @see CreditBill
 */
public interface CreditBillComponent {
    
    // 保存信用额度消费
    int saveCreditBill(Long customerId, BigDecimal amount, String remark);
    
    // 根据用户id查询可用信用额度
    BigDecimal getAvailableCredit(Long customerId);
}
