package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.SmsBill;
import com.adbest.smsmarketingfront.dao.CustomerMarketSettingDao;
import com.adbest.smsmarketingfront.dao.SmsBillDao;
import com.adbest.smsmarketingfront.service.SmsBillComponent;
import com.adbest.smsmarketingfront.service.param.GetSmsBillPage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.ResourceBundle;


@Component
@Slf4j
public class SmsBillComponentImpl implements SmsBillComponent {
    
    @Autowired
    SmsBillDao smsBillDao;
    @Autowired
    CustomerMarketSettingDao customerMarketSettingDao;
    
    @Autowired
    ResourceBundle bundle;
    @Autowired
    JPAQueryFactory jpaQueryFactory;
    
    @Transactional
    @Override
    public int saveSmsBill(Long customerId, String describe, Integer amount) {
        log.info("enter saveSmsBill, customerId={} describe={} amount={}", customerId, describe, amount);
        Assert.notNull(customerId, "customerId can't be null");
        Assert.hasText(describe, "describe can't be empty!");
        SmsBill smsBill = new SmsBill();
        smsBill.setCustomerId(customerId);
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
    
    @Override
    @Transactional
    public void saveAll(List<SmsBill> smsBills) {
        smsBillDao.saveAll(smsBills);
    }
}
