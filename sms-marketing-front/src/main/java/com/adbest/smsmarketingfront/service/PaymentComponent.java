package com.adbest.smsmarketingfront.service;

import java.math.BigDecimal;

/**
 * 用户支付业务组件
 */
public interface PaymentComponent {
    
    /**
     * 实时提交账单，若失败则返回提示
     * @param customerId 用户id
     * @param cost 支付金额(>0)
     */
    void realTimePayment(Long customerId, BigDecimal cost);
    
    // 推送指定用户账单
    void paymentRequest(Long customerId);
}
