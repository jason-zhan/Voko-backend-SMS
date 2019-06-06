package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.SmsBill;
import com.adbest.smsmarketingfront.dao.SmsBillDao;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.SmsBillComponent;
import com.adbest.smsmarketingfront.service.param.GetSmsBillPage;
import com.adbest.smsmarketingfront.util.Current;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.util.ResourceBundle;


@Component
@Slf4j
public class SmsBillComponentImpl implements SmsBillComponent {
    
    @Autowired
    SmsBillDao smsBillDao;
    
    @Autowired
    ResourceBundle bundle;
    @Autowired
    JPAQueryFactory jpaQueryFactory;
    
    @Override
    public synchronized int saveSmsBill(String describe, Integer amount) {
        log.info("enter saveSmsBill, describe=" + describe + ", amount=" + amount);
        Assert.hasText(describe, "describe can't be empty!");
        Assert.notNull(amount, "amount can't be empty!");
        if (amount == 0) {
            return 0;
        }
        Long curId = Current.get().getId();
//        Long curId = 1L;
        if (amount < 0) {
            Long sum = smsBillDao.sumByCustomerId(curId);
            ServiceException.isTrue(sum + amount >= 0, bundle.getString("sms-balance-not-enough"));
        }
        SmsBill smsBill = new SmsBill();
        smsBill.setCustomerId(curId);
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
    
    @Override
    public HSSFWorkbook findByConditionsToExcel(GetSmsBillPage getSmsBillPage) {
        log.info("enter findByConditionsToExcel, param={}", getSmsBillPage);
        
        log.info("leave findByConditionsToExcel");
        return null;
    }
}
