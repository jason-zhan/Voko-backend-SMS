package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.FinanceBill;
import com.adbest.smsmarketingentity.FinanceBillStatus;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.dao.FinanceBillDao;
import com.adbest.smsmarketingfront.service.PaymentComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.ResourceBundle;

@Component
@Slf4j
public class PaymentComponentImpl implements PaymentComponent {
    
    @Autowired
    CustomerDao customerDao;
    @Autowired
    FinanceBillDao financeBillDao;
    
    @Autowired
    ResourceBundle bundle;
    
    @Override
    public void realTimePayment(Long customerId, BigDecimal cost, String remark) {
        log.info("enter realTimePayment, customerId={}, cost={}", customerId, cost);
        // TODO amount 校验
//        Assert.isTrue(cost != null && cost.compareTo(BigDecimal.ZERO) > 0, "cost must be greater than zero.");
        // TODO 发送远程扣款请求
        Assert.hasText(remark, "remark is empty.");
        FinanceBill bill = new FinanceBill();
        bill.setCustomerId(customerId);
        bill.setAmount(cost);
        bill.setStatus(FinanceBillStatus.SUBMITTED.getValue());
        bill.setInfoDescribe(remark);
        financeBillDao.save(bill);
        log.info("leave realTimePayment");
    }
    
    @Override
    public void paymentRequest(Long customerId) {
        log.info("enter paymentRequest, customerId={}", customerId);
        
        
        log.info("leave paymentRequest");
    }
}
