package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.MmsBill;
import com.adbest.smsmarketingfront.dao.MmsBillDao;
import com.adbest.smsmarketingfront.service.MmsBillComponent;
import com.adbest.smsmarketingfront.util.Current;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;


@Component
@Slf4j
public class MmsBillComponentImpl implements MmsBillComponent {
    
    @Autowired
    MmsBillDao mmsBillDao;
    
    @Override
    public int saveMmsBill(String describe, Integer amount) {
        log.info("enter saveMmsBill, describe=" + describe + ", amount=" + amount);
        Assert.hasText(describe, "describe can't be empty!");
        Assert.notNull(amount, "amount can't be empty!");
        MmsBill mmsBill = new MmsBill();
        mmsBill.setCustomerId(Current.getUserDetails().getId());
        mmsBill.setInfoDescribe(describe);
        mmsBill.setAmount(amount);
        mmsBillDao.save(mmsBill);
        log.info("leave saveMmsBill");
        return 1;
    }
}
