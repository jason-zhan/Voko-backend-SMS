package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.CreditBill;
import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingfront.dao.CreditBillDao;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.CreditBillComponent;
import com.adbest.smsmarketingfront.util.CommonMessage;
import com.adbest.smsmarketingfront.util.Current;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.ResourceBundle;

@Service
@Slf4j
public class CreditBillComponentImpl implements CreditBillComponent {
    
    @Autowired
    CreditBillDao creditBillDao;
    @Autowired
    CustomerDao customerDao;
    
    @Autowired
    ResourceBundle bundle;
    
    @Override
    public int saveCreditBill(Long customerId, BigDecimal amount, String remark) {
        log.info("enter saveCreditBill, customerId={}, amount={}, remark={}", customerId, amount, remark);
        Assert.notNull(customerId, "customerId is null");
        Assert.notNull(amount, "amount is null");
        Assert.hasText(remark, "remark is empty");
        synchronized (Current.get()){
            BigDecimal availableCredit = getAvailableCredit(customerId);
            ServiceException.isTrue(availableCredit.add(amount).compareTo(BigDecimal.ZERO) >= 0, bundle.getString("credit-not-enough"));
            CreditBill creditBill = new CreditBill(customerId, amount, remark);
            creditBillDao.save(creditBill);
        }
        log.info("leave saveCreditBill");
        return 1;
    }
    
    @Override
    public BigDecimal getAvailableCredit(Long customerId) {
        log.info("enter getAvailableCredit, customerId={}", customerId);
        Assert.notNull(customerId, CommonMessage.PARAM_IS_INVALID);
        Optional<Customer> optional = customerDao.findById(customerId);
        Assert.isTrue(optional.isPresent(), CommonMessage.OBJECT_NOT_FOUND);
        Customer customer = optional.get();
        BigDecimal usedAmount = creditBillDao.sumAmountByCustomerId(customerId);
        log.info("leave getAvailableCredit");
        return customer.getCredit().add(usedAmount);
    }
}
