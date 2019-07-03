package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.FinanceBill;
import com.adbest.smsmarketingfront.dao.FinanceBillDao;
import com.adbest.smsmarketingfront.service.FinanceBillComponent;
import com.adbest.smsmarketingfront.service.PaymentComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@Component
@Slf4j
public class FinanceBillComponentImpl implements FinanceBillComponent {
    
    @Autowired
    FinanceBillDao financeBillDao;

    @Autowired
    private PaymentComponent paymentComponent;
    
    @Override
    public int saveFinanceBill(BigDecimal amount, String description) {
        return 0;
    }

    @Override
    @Transactional
    public Boolean realTimeDeduction(BigDecimal amount, String description, Long customerId) {
        paymentComponent.realTimePayment(customerId, amount);
        FinanceBill financeBill = new FinanceBill();
        financeBill.setAmount(amount);
        financeBill.setCustomerId(customerId);
        financeBill.setInfoDescribe(description);
        financeBillDao.save(financeBill);
        return true;
    }
}
