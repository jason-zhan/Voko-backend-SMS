package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.QSmsBill;
import com.adbest.smsmarketingentity.SmsBill;
import com.adbest.smsmarketingfront.dao.SmsBillDao;
import com.adbest.smsmarketingfront.service.SmsBillService;
import com.adbest.smsmarketingfront.service.param.GetMsgBillPage;
import com.adbest.smsmarketingfront.util.CommonMessage;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.PageBase;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Slf4j
public class SmsBillServiceImpl implements SmsBillService {
    
    @Autowired
    private SmsBillDao smsBillDao;
    @Autowired
    private JPAQueryFactory queryFactory;
    
    @Override
    public SmsBill findById(Long id) {
        log.info("enter findById, id=" + id);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        SmsBill smsBill = smsBillDao.findByIdAndCustomerId(id, Current.get().getId());
        log.info("leave findById");
        return smsBill;
    }
    
    @Override
    public Page<SmsBill> findByConditions(GetMsgBillPage getBillPage) {
        log.info("enter findByConditions, param={}", getBillPage);
        Assert.notNull(getBillPage, CommonMessage.PARAM_IS_NULL);
        BooleanBuilder builder = new BooleanBuilder();
        QSmsBill qSmsBill = QSmsBill.smsBill;
        getBillPage.fillConditions(builder, qSmsBill);
        QueryResults<SmsBill> queryResults = queryFactory.select(qSmsBill).from(qSmsBill)
                .where(builder)
                .orderBy(qSmsBill.time.desc())
                .offset(getBillPage.getPage() * getBillPage.getSize())
                .limit(getBillPage.getSize())
                .fetchResults();
        Page<SmsBill> billPage = PageBase.toPageEntity(queryResults, getBillPage);
        log.info("leave findByConditions");
        return billPage;
    }

    @Override
    public Long sumByCustomerId(Long customerId) {
        return smsBillDao.sumByCustomerId(customerId);
    }

    @Override
    @Transactional
    public void saveAll(List<SmsBill> smsBills) {
        smsBillDao.saveAll(smsBills);
    }
}
