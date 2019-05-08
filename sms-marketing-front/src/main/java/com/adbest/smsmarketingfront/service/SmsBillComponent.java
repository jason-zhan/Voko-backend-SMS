package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.SmsBill;

/**
 * 短信账单业务组件
 * @see SmsBill
 */
public interface SmsBillComponent {
    
    // 产生一条短信账单
    int saveSmsBill(String describe, Integer amount);
}
