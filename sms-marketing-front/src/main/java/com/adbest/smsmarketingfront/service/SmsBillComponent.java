package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.SmsBill;
import com.adbest.smsmarketingfront.service.param.GetSmsBillPage;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.data.domain.Page;

/**
 * 短信账单业务组件
 * @see SmsBill
 */
public interface SmsBillComponent {
    
    // 产生一条短信账单
    int saveSmsBill(String describe, Integer amount);

    SmsBill save(SmsBill smsBill);
    
    // 根据查询条件查询短信账单
    Page<SmsBill> findByConditions(GetSmsBillPage getSmsBillPage);
    
    // 根据查询条件生成短信账单报表
    HSSFWorkbook findByConditionsToExcel(GetSmsBillPage getSmsBillPage);
    
}
