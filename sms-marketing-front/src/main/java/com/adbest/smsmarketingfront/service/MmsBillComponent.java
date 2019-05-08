package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MmsBill;

/**
 * 彩信账单业务组件
 * @see MmsBill
 */
public interface MmsBillComponent {
    
    // 产生一条彩信账单
    int saveMmsBill(String describe, Integer amount);
    
}
