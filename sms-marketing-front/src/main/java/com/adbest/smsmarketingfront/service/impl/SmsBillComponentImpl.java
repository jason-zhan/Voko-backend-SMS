package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.SmsBill;
import com.adbest.smsmarketingfront.dao.SmsBillDao;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.SmsBillComponent;
import com.adbest.smsmarketingfront.util.Current;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Syntax;
import javax.transaction.Transactional;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;


@Component
@Slf4j
public class SmsBillComponentImpl implements SmsBillComponent {
    
    @Autowired
    SmsBillDao smsBillDao;
    @Autowired
    ResourceBundle bundle;
    
    @Override
    public synchronized int saveSmsBill(String describe, Integer amount) {
        log.info("enter saveSmsBill, describe=" + describe + ", amount=" + amount);
        Assert.hasText(describe, "describe can't be empty!");
        Assert.notNull(amount, "amount can't be empty!");
        if (amount == 0) {
            return 0;
        }
//        Long curId = Current.get().getId();
        Long curId = 1L;
        if (amount < 0) {
            Long sum = smsBillDao.sumByCustomerId(curId);
            ServiceException.isTrue(sum + amount >= 0, bundle.getString("sms-balance-not-enough"));
        }
        SmsBill smsBill = new SmsBill();
        smsBill.setCustomerId(curId);
//        smsBill.setCustomerId(1L);
        smsBill.setInfoDescribe(describe);
        smsBill.setAmount(amount);
        smsBillDao.save(smsBill);
        log.info("leave saveSmsBill");
        return 1;
    }
    
    @Override
    @Transactional
    public SmsBill save(SmsBill smsBill) {
        return smsBillDao.save(smsBill);
    }
}
