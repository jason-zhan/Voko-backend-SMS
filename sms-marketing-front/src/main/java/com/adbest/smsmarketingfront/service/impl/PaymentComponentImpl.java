package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.dao.FinanceBillDao;
import com.adbest.smsmarketingfront.entity.enums.VokoPayStatus;
import com.adbest.smsmarketingfront.entity.vo.VokoPayVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.CreditBillComponent;
import com.adbest.smsmarketingfront.service.CustomerService;
import com.adbest.smsmarketingfront.service.PaymentComponent;
import com.adbest.smsmarketingfront.util.VokophonePayUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
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

    @Autowired
    private CreditBillComponent creditBillComponent;
    
    @Override
    public void realTimePayment(Long customerId, BigDecimal cost, String remark) {
        log.info("enter realTimePayment, customerId={}, cost={}", customerId, cost);
        // TODO amount 校验
//        Assert.isTrue(cost != null && cost.compareTo(BigDecimal.ZERO) > 0, "cost must be greater than zero.");
        Customer customer = customerService.findById(customerId);
        // TODO 发送远程扣款请求
        VokoPayVo pay = vokophonePayUtils.pay(customer.getCustomerLogin(), remark, cost);
        if (!VokoPayStatus.SUCCESS_TRANSACTION.getStatusCode().equals((pay.getStatus_code()))){
            VokoPayStatus vokoPayStatus = VokoPayStatus.getVokoPayStatus(pay.getStatus_code());
            ServiceException.isTrue(false, vokoPayStatus==null?"Payment failed":vokoPayStatus.getMsg());
        }
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
    @Transactional
    public void pushMonthlyBills(List<CreditBill> cBills) {
        BigDecimal sum = cBills.stream().map(CreditBill::getAmount)
                .reduce(BigDecimal::add)
                .get();
        if (sum.doubleValue()>=0){
            return;
        }
        String remark = bundle.getString("SMS_PLATFORM_MONTHLY_CONSUMPTION");
        CreditBill creditBill = cBills.get(0);
        Customer customer = customerService.findById(creditBill.getCustomerId());
        VokoPayVo pay = null;
        try {
            pay = vokophonePayUtils.pay(customer.getCustomerLogin(), remark, sum.negate());
        }catch (Exception e){
            log.error("Voko Deduction error,{},{}",customer.getId(),e);
            return;
        }
        String rem = null;
        int chargingStatus = 0;
        if (!VokoPayStatus.SUCCESS_TRANSACTION.getStatusCode().equals((pay.getStatus_code()))){
            VokoPayStatus vokoPayStatus = VokoPayStatus.getVokoPayStatus(pay.getStatus_code());
            rem = vokoPayStatus.getStatusCode()+"_"+vokoPayStatus.getMsg();
            chargingStatus = CreditBillChargingStatus.FAILURE_DEDUCT_FEES.getValue();
        }else {
            FinanceBill bill = new FinanceBill();
            bill.setCustomerId(customer.getId());
            bill.setAmount(sum.negate());
            bill.setStatus(FinanceBillStatus.SUBMITTED.getValue());
            bill.setInfoDescribe(remark);
            financeBillDao.save(bill);
            creditBillComponent.resumeAvailableCredit(customer.getId(), sum.negate(), remark);
            chargingStatus = CreditBillChargingStatus.SUCCESSFUL_DEDUCTION.getValue();
        }
        for (CreditBill cBill : cBills) {
            cBill.setPayRemarks(rem);
            cBill.setChargingStatus(chargingStatus);
        }
        creditBillComponent.saveAll(cBills);
    }
}
