package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.QContacts;
import com.adbest.smsmarketingentity.QMessageRecord;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.MessageRecordService;
import com.adbest.smsmarketingfront.service.param.GetInboxMessagePage;
import com.adbest.smsmarketingfront.service.param.GetOutboxMessagePage;
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

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Service
@Slf4j
public class MessageRecordServiceImpl implements MessageRecordService {
    
    @Autowired
    MessageRecordDao messageRecordDao;
    
    @Autowired
    JPAQueryFactory jpaQueryFactory;
    @Autowired
    ResourceBundle bundle;
    
    @Override
    public int deleteOneMessage(Long id) {
        log.info("enter deleteOneMessage, id=" + id);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        Optional<MessageRecord> optional = messageRecordDao.findById(id);
        ServiceException.isTrue(optional.isPresent(), bundle.getString("msg-record-not-exists"));
        Assert.isTrue(Current.get().getId().equals(optional.get().getCustomerId()), bundle.getString("permission denied"));
        int result = messageRecordDao.disableById(id, true);
        log.info("leave deleteOneMessage");
        return result;
    }
    
    @Override
    public int batchDelete(List<Long> idList) {
        return 0;
    }
    
    @Override
    public MessageRecord findById(Long id) {
        log.info("enter findById, id=" + id);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        MessageRecord messageRecord = messageRecordDao.findByIdAndDisableIsFalse(id);
        log.info("leave findById");
        return messageRecord;
    }
    
    @Override
    public Page<MessageRecord> findInboxByConditions(GetInboxMessagePage getInboxPage) {
        log.info("enter findInboxByConditions, param={}", getInboxPage);
        Assert.notNull(getInboxPage, CommonMessage.PARAM_IS_NULL);
        QMessageRecord qMessageRecord = QMessageRecord.messageRecord;
        QContacts qContacts = QContacts.contacts;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qMessageRecord.customerId.eq(Current.get().getId()));
        getInboxPage.fillConditions(builder, qMessageRecord, qContacts);
        QueryResults<MessageRecord> queryResults = jpaQueryFactory.select(qMessageRecord)
                .from(qMessageRecord)
                .leftJoin(qContacts).on(qMessageRecord.contactsId.eq(qContacts.id))
                .where(builder)
                .orderBy(qMessageRecord.status.asc(), qMessageRecord.createTime.desc())
                .offset(getInboxPage.getPage() * getInboxPage.getSize())
                .limit(getInboxPage.getSize())
                .fetchResults();
        Page<MessageRecord> messagePage = PageBase.toPageEntity(queryResults, getInboxPage);
        log.info("leave findInboxByConditions");
        return messagePage;
    }
    
    @Override
    public Page<MessageRecord> findOutboxByConditions(GetOutboxMessagePage getOutboxPage) {
        log.info("enter findOutboxByConditions, param={}", getOutboxPage);
        Assert.notNull(getOutboxPage, CommonMessage.PARAM_IS_NULL);
        QMessageRecord qMessageRecord = QMessageRecord.messageRecord;
        QContacts qContacts = QContacts.contacts;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qMessageRecord.customerId.eq(Current.get().getId()));
        getOutboxPage.fillConditions(builder, qMessageRecord, qContacts);
        QueryResults<MessageRecord> queryResults = jpaQueryFactory.select(qMessageRecord)
                .from(qMessageRecord)
                .leftJoin(qContacts).on(qMessageRecord.contactsId.eq(qContacts.id))
                .where(builder)
                .orderBy(qMessageRecord.sendTime.desc())
                .offset(getOutboxPage.getPage() * getOutboxPage.getSize())
                .limit(getOutboxPage.getSize())
                .fetchResults();
        Page<MessageRecord> messagePage = PageBase.toPageEntity(queryResults, getOutboxPage);
        log.info("leave findOutboxByConditions");
        return messagePage;
    }
}
