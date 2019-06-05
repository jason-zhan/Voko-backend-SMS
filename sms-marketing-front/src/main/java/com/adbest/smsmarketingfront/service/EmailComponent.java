package com.adbest.smsmarketingfront.service;

/**
 * 邮件发送业务
 */
public interface EmailComponent {
    
    // 发送套餐余量提醒
    void sendPackageRemainingTip(String toAddress, int smsRemaining);
    
    // 发送月度金融账单
    void sendMonthlyBill(Long customerId);
}
