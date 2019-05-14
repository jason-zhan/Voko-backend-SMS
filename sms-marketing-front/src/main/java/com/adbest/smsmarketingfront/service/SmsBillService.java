package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.SmsBill;
import com.adbest.smsmarketingfront.service.param.GetMsgBillPage;
import org.springframework.data.domain.Page;

/**
 * 短信消费账单业务
 * @see SmsBill
 */
public interface SmsBillService {
    
    // 根据id查询
    SmsBill findById(Long id);
    
    // 根据条件查询列表
    Page<SmsBill> findByConditions(GetMsgBillPage getBillPage);
}
