package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.CreditBill;
import com.adbest.smsmarketingentity.CreditBillChargingStatus;
import com.adbest.smsmarketingentity.CreditBillType;
import com.adbest.smsmarketingfront.dao.CreditBillDao;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.service.CreditBillComponent;
import com.adbest.smsmarketingfront.util.Current;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.List;
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
    @Transactional
    public void adjustCustomerMaxCredit(Long customerId, BigDecimal amount) {
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
        creditBill.setChargingStatus(CreditBillChargingStatus.NO_PUSH_REQUIRED.getValue());
        creditBillDao.save(creditBill);
        log.info("leave adjustCustomerMaxCredit");
    }

    @Override
    @Transactional
    public boolean cancellationQuota(List<Long> customerIds, BigDecimal amount) {
        if (customerIds.size()<=0){return true;}
        CreditBill creditBill = null;
        for (Long customerId: customerIds) {
            int updateMaxCredit = customerDao.updateMaxCredit(customerId, amount);
            if (updateMaxCredit>0){
                creditBill = new CreditBill();
                creditBill.setCustomerId(customerId);
                creditBill.setType(CreditBillType.ADJUST_MAX_CREDIT.getValue());
                creditBill.setReferId(customerId);
                creditBill.setAmount(amount);
                creditBill.setChargingStatus(CreditBillChargingStatus.UNPUSHED.getValue());
                creditBill.setRemark(bundle.getString("adjust-max-credit"));
                creditBillDao.save(creditBill);
            }
        }
        log.info("leave cancellationQuota");
        return true;
    }
    
    @Transactional
    @Override
    public void savePlanConsume(Long customerId, Long planId, BigDecimal amount, String remark) {
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
        bill.setChargingStatus(CreditBillChargingStatus.UNPUSHED.getValue());
        creditBillDao.save(bill);
        log.info("leave savePlanConsume");
    }
    
    @Override
    public void saveKeywordConsume(Long keywordId, BigDecimal amount, String remark) {
        log.info("enter saveKeywordConsume, keywordId={}, amount={}, remark={}", keywordId, amount, remark);
        Assert.notNull(keywordId, "keywordId is null");
        Assert.isTrue(amount != null && amount.compareTo(BigDecimal.ZERO) < 0, "amount must be less than zero.");
        Assert.hasText(remark, "remark is empty");
        int updateResult = customerDao.paymentByCredit(Current.get().getId(), amount);
        Assert.isTrue(updateResult > 0, "The available credit of customer is less than zero.");
        CreditBill bill = new CreditBill();
        bill.setCustomerId(Current.get().getId());
        bill.setType(CreditBillType.KEYWORD.getValue());
        bill.setReferId(keywordId);
        bill.setAmount(amount);
        bill.setChargingStatus(CreditBillChargingStatus.UNPUSHED.getValue());
        bill.setRemark(remark);
        creditBillDao.save(bill);
        log.info("leave saveKeywordConsume");
    }
    
    @Override
    public void saveCustomerMobileConsume(Long customerId, Long mobileNumberId, BigDecimal amount, String remark) {
        log.info("enter saveCustomerMobileConsume, customerId={}, mobileNumberId={}, amount={}, remark={}", customerId, mobileNumberId, amount, remark);
        Assert.notNull(customerId, "customerId is null");
        Assert.notNull(mobileNumberId, "mobileNumberId is null");
        Assert.isTrue(amount != null && amount.compareTo(BigDecimal.ZERO) < 0, "amount must be less than zero.");
        Assert.hasText(remark, "remark is empty");
        int updateResult = customerDao.paymentByCredit(customerId, amount);
        Assert.isTrue(updateResult > 0, "customer is not exists, or the available credit of customer is less than zero.");
        CreditBill bill = new CreditBill();
        bill.setCustomerId(customerId);
        bill.setType(CreditBillType.CUSTOMER_MOBILE.getValue());
        bill.setReferId(mobileNumberId);
        bill.setAmount(amount);
        bill.setChargingStatus(CreditBillChargingStatus.UNPUSHED.getValue());
        bill.setRemark(remark);
        creditBillDao.save(bill);
        log.info("leave saveCustomerMobileConsume");
    }
    
    @Override
    public void resumeAvailableCredit(Long customerId, BigDecimal amount, String remark) {
        log.info("enter resumeAvailableCredit, customerId={}, amount={}", customerId, amount);
        Assert.notNull(customerId, "customerId is null");
        Assert.isTrue(amount != null && amount.compareTo(BigDecimal.ZERO) > 0, "amount must be greater than zero.");
        Assert.notNull(remark, "remark can't be null.");
        int updateResult = customerDao.resumeCredit(customerId, amount);
        Assert.isTrue(updateResult > 0, "customer is not exists");
        CreditBill bill = new CreditBill();
        bill.setCustomerId(customerId);
        bill.setType(CreditBillType.RESUME_AVAILABLE_CREDIT.getValue());
        bill.setReferId(customerId);
        bill.setAmount(amount);
        bill.setRemark(remark);
        bill.setChargingStatus(CreditBillChargingStatus.NO_PUSH_REQUIRED.getValue());
        creditBillDao.save(bill);
        log.info("leave resumeAvailableCredit");
    }

    @Override
    @Transactional
    public void saveAll(List<CreditBill> cBills) {
        creditBillDao.saveAll(cBills);
    }
}
