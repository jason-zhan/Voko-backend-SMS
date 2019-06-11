package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.dao.FinanceBillDao;
import com.adbest.smsmarketingfront.service.EmailComponent;
import com.adbest.smsmarketingfront.util.EasyTime;
import com.adbest.smsmarketingfront.util.EmailTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class EmailComponentImpl implements EmailComponent {
    
    @Autowired
    EmailTools emailTools;
    
    @Autowired
    CustomerDao customerDao;
    @Autowired
    FinanceBillDao financeBillDao;
    
    @Value("${web.login}")
    private String webLogin;
    
    @Override
    public void sendPackageRemainingTip(String toAddress, int smsRemaining) {
        log.info("enter sendPackageRemainingTip, toAddress={}, smsRemaining={}", toAddress, smsRemaining);
        Assert.hasText(toAddress, "toAddress is empty");
        Map<String, Object> map = new HashMap<>();
        map.put("smsRemaining", smsRemaining);
        map.put("time", EasyTime.now());
        emailTools.send("Package Remaining Tip", toAddress, "./doc/email/package-remaining-tip", map);
        log.info("leave sendPackageRemainingTip");
    }
    
    @Override
    public void sendMonthlyBill(Long customerId) {
        log.info("enter sendMonthlyBill, customerId={}", customerId);
        Assert.notNull(customerId, "customerId is empty");
        Optional<Customer> optional = customerDao.findById(customerId);
        Assert.isTrue(optional.isPresent(), "customer not exists!");
        Customer customer = optional.get();
        Map<String, Object> data = new HashMap<>();
        BigDecimal monthAmount = financeBillDao.sumWithCustomerIdAndTimeRange(
                customerId,
                EasyTime.init().addMonths(-1).monthStart().stamp(),
                EasyTime.init().addMonths(-1).monthEnd().stamp()
        );
        data.put("monthAmount", monthAmount);
        data.put("webLogin", webLogin);
        emailTools.send("Monthly Bill", customer.getEmail(), "./doc/email/monthly-financial-bill", data);
        log.info("leave sendMonthlyBill");
    }
}
