package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.FinanceBill;
import com.adbest.smsmarketingentity.FinanceBillStatus;
import com.adbest.smsmarketingfront.dao.FinanceBillDao;
import com.adbest.smsmarketingfront.service.FinanceBillComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.math.BigDecimal;

@Component
@Slf4j
public class FinanceBillComponentImpl implements FinanceBillComponent {
    
    @Autowired
    FinanceBillDao financeBillDao;
    
    @Override
    public int saveFinanceBill(Long customerId, BigDecimal cost, String description) {
        log.info("enter saveFinanceBill, customerId={}, cost={}, description={}", customerId, cost, description);
        Assert.isTrue(cost != null && cost.compareTo(BigDecimal.ZERO) != 0, "cost can't be null or zero.");
        Assert.hasText(description, "description is empty.");
        FinanceBill bill = new FinanceBill();
        bill.setCustomerId(customerId);
        bill.setInfoDescribe(description);
        bill.setAmount(cost);
        bill.setStatus(FinanceBillStatus.UNSUBMITTED.getValue());
        financeBillDao.save(bill);
        log.info("leave saveFinanceBill");
        return 1;
    }
}
