package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.CreditBill;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户支付业务组件
 */
public interface PaymentComponent {
    
    /**
     * 实时提交账单，若失败则返回提示
     * @param customerId 用户id
     * @param cost 支付金额(>0)
     */
    void realTimePayment(Long customerId, BigDecimal cost, String remark);

    void pushMonthlyBills(List<CreditBill> cBills);
}
