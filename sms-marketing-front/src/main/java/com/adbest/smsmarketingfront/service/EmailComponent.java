package com.adbest.smsmarketingfront.service;

/**
 * 邮件发送业务
 */
public interface EmailComponent {
    
    // 发送套餐余量提醒
    void sendPackageRemainingTip(String toAddress, int smsRemaining);
    
    // 发送月度账单(附件)
    void sendMonthlyBill(Long customerId);
}
