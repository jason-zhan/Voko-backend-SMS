package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingfront.dao.FinanceBillDao;
import com.adbest.smsmarketingfront.service.FinanceBillComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class FinanceBillComponentImpl implements FinanceBillComponent {
    
    @Autowired
    FinanceBillDao financeBillDao;
    
    @Override
    public int saveFinanceBill(BigDecimal amount, String description) {
        // todo
        
        return 0;
    }
}
