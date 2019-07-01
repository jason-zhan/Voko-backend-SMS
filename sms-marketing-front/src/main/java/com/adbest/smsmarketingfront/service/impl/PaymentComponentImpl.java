package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.handler.ServiceException;
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
    ResourceBundle bundle;
    
    @Override
    public void realTimePayment(Long customerId, BigDecimal cost) {
        log.info("enter realTimePayment, customerId={}, cost={}", customerId, cost);
        Assert.isTrue(cost != null && cost.compareTo(BigDecimal.ZERO) > 0, "cost must be greater than zero.");
        
        log.info("leave realTimePayment");
    }
    
    @Override
    public void paymentRequest(Long customerId) {
        log.info("enter paymentRequest, customerId={}", customerId);
        
        
        log.info("leave paymentRequest");
    }
}
