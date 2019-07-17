package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.FinanceBill;

import java.math.BigDecimal;

/**
 * 金融消费记录业务组件
 * @see FinanceBill
 */
public interface FinanceBillComponent {
    
    /**
     * 生成一条金融记录
     * @param customerId 用户id
     * @param cost  金额 (+-)
     * @param description  描述
     * @return
     */
    int saveFinanceBill(Long customerId, BigDecimal cost, String description);
}
