package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.QSmsBill;
import com.adbest.smsmarketingentity.SmsBill;
import com.adbest.smsmarketingfront.dao.SmsBillDao;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.SmsBillComponent;
import com.adbest.smsmarketingfront.service.param.GetSmsBillPage;
import com.adbest.smsmarketingfront.util.CommonMessage;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.TimeTools;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    
    @Override
    public Page<SmsBill> findByConditions(GetSmsBillPage getSmsBillPage) {
        log.info("enter findByConditions, param={}", getSmsBillPage);
        Assert.notNull(getSmsBillPage, CommonMessage.PARAM_IS_NULL);
        BooleanBuilder builder = new BooleanBuilder();
        QSmsBill qSmsBill = QSmsBill.smsBill;
        getSmsBillPage.fillConditions(builder, qSmsBill);
        QueryResults<SmsBill> queryResults = jpaQueryFactory.select(qSmsBill).from(qSmsBill)
                .where(builder)
                .orderBy(qSmsBill.time.desc())
                .offset(getSmsBillPage.getPage() * getSmsBillPage.getSize())
                .limit(getSmsBillPage.getSize())
                .fetchResults();
        Page<SmsBill> billPage = PageBase.toPageEntity(queryResults, getSmsBillPage);
        log.info("leave findByConditions");
        return billPage;
    }
    
    @Override
    public HSSFWorkbook findByConditionsToExcel(GetSmsBillPage getSmsBillPage) {
        log.info("enter findByConditionsToExcel, param={}", getSmsBillPage);
        Assert.notNull(getSmsBillPage, CommonMessage.PARAM_IS_NULL);
        // 1. 创建工作簿，全局设置
        HSSFWorkbook workbook = new HSSFWorkbook();
        String sheetName = bundle.getString("sms-bill-report") + " - " + TimeTools.formatDateStr("yy-MM-dd HH:mm:ss");
        HSSFSheet sheet = workbook.createSheet(sheetName);
        sheet.setDefaultRowHeightInPoints(30);
        sheet.setDefaultColumnWidth(12);
        // 2. 统计数据，设定表头
        
        // 3. 循环设定行数据
        
        // 4. 成表后调整(可选)
        log.info("leave findByConditionsToExcel");
        return null;
    }
}
