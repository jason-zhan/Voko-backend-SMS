package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.MmsBill;
import com.adbest.smsmarketingfront.dao.MmsBillDao;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.MmsBillComponent;
import com.adbest.smsmarketingfront.util.Current;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.util.List;
import java.util.ResourceBundle;


@Component
@Slf4j
public class MmsBillComponentImpl implements MmsBillComponent {
    
    @Autowired
    MmsBillDao mmsBillDao;
    @Autowired
    ResourceBundle bundle;
    
    @Override
    public synchronized int saveMmsBill(Long customerId, String describe, Integer amount) {
        log.info("enter saveMmsBill, describe=" + describe + ", amount=" + amount);
        Assert.notNull(customerId, "customerId can't be null");
        Assert.hasText(describe, "describe can't be empty!");
        Assert.notNull(amount, "amount can't be empty!");
        if (amount == 0) {
            return 0;
        }
        if (amount < 0) {
            Long sum = mmsBillDao.sumByCustomerId(customerId);
            ServiceException.isTrue(sum + amount >= 0, bundle.getString("mms-balance-not-enough"));
        }
        MmsBill mmsBill = new MmsBill();
        mmsBill.setCustomerId(customerId);
        mmsBill.setInfoDescribe(describe);
        mmsBill.setAmount(amount);
        mmsBillDao.save(mmsBill);
        log.info("leave saveMmsBill");
        return 1;
    }
    
    @Override
    @Transactional
    public MmsBill save(MmsBill mmsBill) {
        return mmsBillDao.save(mmsBill);
    }

    @Override
    @Transactional
    public void saveAll(List<MmsBill> mmsBills) {
        mmsBillDao.saveAll(mmsBills);
    }
}
