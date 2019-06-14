package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MmsBill;
import com.adbest.smsmarketingentity.SmsBill;
import com.adbest.smsmarketingfront.service.param.GetMsgBillPage;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 彩信消费账单业务
 * @see MmsBill
 */
public interface MmsBillService {
    
    // 根据id查询
    MmsBill findById(Long id);
    
    // 根据条件查询列表
    Page<MmsBill> findByConditions(GetMsgBillPage getBillPage);

    Long sumByCustomerId(Long customerId);

    void saveAll(List<MmsBill> mmsBills);
}
