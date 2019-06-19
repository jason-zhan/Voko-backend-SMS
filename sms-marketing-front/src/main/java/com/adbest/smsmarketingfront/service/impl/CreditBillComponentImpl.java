package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.CreditBill;
import com.adbest.smsmarketingentity.CreditBillType;
import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingfront.dao.CreditBillDao;
import com.adbest.smsmarketingfront.service.CreditBillComponent;
import com.adbest.smsmarketingfront.util.CommonMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.ResourceBundle;

@Component
@Slf4j
public class CreditBillComponentImpl implements CreditBillComponent {
    
    @Autowired
    CreditBillDao creditBillDao;
    
    @Autowired
    ResourceBundle bundle;
    
    @Override
    public int adjustCustomerMaxCredit(Long customerId, BigDecimal amount) {
        log.info("enter adjustCustomerMaxCredit, customerId={}, amount={}", customerId, amount);
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) != 0, "amount must not be zero.");
        CreditBill creditBill = new CreditBill();
        creditBill.setCustomerId(customerId);
        creditBill.setType(CreditBillType.ADJUST_MAX_CREDIT.getValue());
        creditBill.setReferId(customerId);
        creditBill.setAmount(amount);
        creditBill.setRemark(bundle.getString("adjust-max-credit"));
        creditBillDao.save(creditBill);
        log.info("leave adjustCustomerMaxCredit");
        return 1;
    }
    
    @Override
    public int savePlanConsume(Long customerId, Long planId, BigDecimal amount, String remark) {
        log.info("enter savePlanConsume, customerId={}, planId={}, amount={}, remark={}", customerId, planId, amount, remark);
        
        log.info("leave savePlanConsume");
        return 0;
    }
}
