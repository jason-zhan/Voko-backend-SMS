package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingentity.FinanceBill;
import com.adbest.smsmarketingentity.FinanceBillStatus;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.dao.FinanceBillDao;
import com.adbest.smsmarketingfront.entity.enums.VokoPayStatus;
import com.adbest.smsmarketingfront.entity.vo.VokoPayVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.CustomerService;
import com.adbest.smsmarketingfront.service.PaymentComponent;
import com.adbest.smsmarketingfront.util.VokophonePayUtils;
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

    @Autowired
    private VokophonePayUtils vokophonePayUtils;

    @Autowired
    private CustomerService customerService;
    
    @Override
    public void realTimePayment(Long customerId, BigDecimal cost, String remark) {
        log.info("enter realTimePayment, customerId={}, cost={}", customerId, cost);
        // TODO amount 校验
//        Assert.isTrue(cost != null && cost.compareTo(BigDecimal.ZERO) > 0, "cost must be greater than zero.");
        Customer customer = customerService.findById(customerId);
        VokoPayVo pay = vokophonePayUtils.pay(customer.getCustomerLogin(), remark, cost);
        if (!VokoPayStatus.SUCCESS_TRANSACTION.getStatusCode().equals((pay.getStatus_code()))){
            VokoPayStatus vokoPayStatus = VokoPayStatus.getVokoPayStatus(pay.getStatus_code());
            ServiceException.isTrue(false, vokoPayStatus==null?"Payment failed":vokoPayStatus.getMsg());
        }
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
