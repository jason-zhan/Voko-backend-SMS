package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.CreditBill;
import com.adbest.smsmarketingentity.CreditBillType;
import com.adbest.smsmarketingfront.dao.CreditBillDao;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.CreditBillComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.ResourceBundle;

@Component
@Slf4j
public class CreditBillComponentImpl implements CreditBillComponent {
    
    @Autowired
    CustomerDao customerDao;
    @Autowired
    CreditBillDao creditBillDao;
    
    @Autowired
    ResourceBundle bundle;
    
    @Override
    public boolean adjustCustomerMaxCredit(Long customerId, BigDecimal amount) {
        log.info("enter adjustCustomerMaxCredit, customerId={}, amount={}", customerId, amount);
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) != 0, "amount must not be zero.");
        int updateMaxCredit = customerDao.updateMaxCredit(customerId, amount);
        Assert.isTrue(updateMaxCredit > 0, "customer is not exists, or the sum of maxCredit and amount is less than zero.");
        CreditBill creditBill = new CreditBill();
        creditBill.setCustomerId(customerId);
        creditBill.setType(CreditBillType.ADJUST_MAX_CREDIT.getValue());
        creditBill.setReferId(customerId);
        creditBill.setAmount(amount);
        creditBill.setRemark(bundle.getString("adjust-max-credit"));
        creditBillDao.save(creditBill);
        log.info("leave adjustCustomerMaxCredit");
        return true;
    }
    
    @Transactional
    @Override
    public boolean savePlanConsume(Long customerId, Long planId, BigDecimal amount, String remark) {
        log.info("enter savePlanConsume, customerId={}, planId={}, amount={}, remark={}", customerId, planId, amount, remark);
        Assert.notNull(customerId, "customerId is null");
        Assert.notNull(planId, "planId is null");
        Assert.isTrue(amount != null && amount.compareTo(BigDecimal.ZERO) != 0, "amount can't be null or zero.");
        Assert.hasText(remark, "remark is empty");
        CreditBill bill = new CreditBill();
        bill.setCustomerId(customerId);
        bill.setType(CreditBillType.MESSAGE_PLAN.getValue());
        bill.setReferId(planId);
        bill.setAmount(amount);
        bill.setRemark(remark);
        creditBillDao.save(bill);
        int creditResult = customerDao.updateCredit(customerId, amount);
        ServiceException.isTrue(creditResult > 0, bundle.getString("credit-not-enough"));
        log.info("leave savePlanConsume");
        return true;
    }
}
