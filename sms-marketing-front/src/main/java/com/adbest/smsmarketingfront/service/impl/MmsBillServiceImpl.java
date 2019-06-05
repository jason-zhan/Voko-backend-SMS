package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.MmsBill;
import com.adbest.smsmarketingentity.QMmsBill;
import com.adbest.smsmarketingfront.dao.MmsBillDao;
import com.adbest.smsmarketingfront.service.MmsBillService;
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

@Service
@Slf4j
public class MmsBillServiceImpl implements MmsBillService {
    
    @Autowired
    MmsBillDao mmsBillDao;
    
    @Autowired
    JPAQueryFactory queryFactory;
    
    @Override
    public MmsBill findById(Long id) {
        log.info("enter findById, id=" + id);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        MmsBill mmsBill = mmsBillDao.findByIdAndCustomerId(id, Current.get().getId());
        log.info("leave findById");
        return mmsBill;
    }
    
    @Override
    public Page<MmsBill> findByConditions(GetMsgBillPage getBillPage) {
        log.info("enter findByConditions, param={}", getBillPage);
        Assert.notNull(getBillPage, CommonMessage.PARAM_IS_NULL);
        BooleanBuilder builder = new BooleanBuilder();
        QMmsBill qMmsBill = QMmsBill.mmsBill;
        getBillPage.fillConditions(builder, qMmsBill);
        QueryResults<MmsBill> queryResults = queryFactory.select(qMmsBill).from(qMmsBill)
                .where(builder)
                .orderBy(qMmsBill.time.desc())
                .offset(getBillPage.getPage() * getBillPage.getSize())
                .limit(getBillPage.getSize())
                .fetchResults();
        Page<MmsBill> billPage = PageBase.toPageEntity(queryResults, getBillPage);
        log.info("leave findByConditions");
        return billPage;
    }
}
