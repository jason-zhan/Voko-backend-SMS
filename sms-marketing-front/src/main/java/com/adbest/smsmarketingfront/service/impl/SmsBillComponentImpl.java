package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.SmsBill;
import com.adbest.smsmarketingfront.dao.SmsBillDao;
import com.adbest.smsmarketingfront.service.SmsBillComponent;
import com.adbest.smsmarketingfront.util.Current;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;


@Component
@Slf4j
public class SmsBillComponentImpl implements SmsBillComponent {
    
    @Autowired
    SmsBillDao smsBillDao;
    
    @Override
    public int saveSmsBill(String describe, Integer amount) {
        log.info("enter saveSmsBill, describe=" + describe + ", amount=" + amount);
        Assert.hasText(describe, "describe can't be empty!");
        Assert.notNull(amount, "amount can't be empty!");
        SmsBill smsBill = new SmsBill();
        smsBill.setCustomerId(Current.get().getId());
        smsBill.setInfoDescribe(describe);
        smsBill.setAmount(amount);
        smsBillDao.save(smsBill);
        log.info("leave saveSmsBill");
        return 1;
    }
}
