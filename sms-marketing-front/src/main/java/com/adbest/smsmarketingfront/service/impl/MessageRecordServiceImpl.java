package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingfront.entity.enums.ContactsSource;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.*;
import com.adbest.smsmarketingfront.service.param.GetInboxMessagePage;
import com.adbest.smsmarketingfront.service.param.GetOutboxMessagePage;
import com.adbest.smsmarketingfront.util.CommonMessage;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.UrlTools;
import com.adbest.smsmarketingfront.util.twilio.MessageTools;
import com.adbest.smsmarketingfront.util.twilio.TwilioUtil;
import com.adbest.smsmarketingfront.util.twilio.param.InboundMsg;
import com.adbest.smsmarketingfront.util.twilio.param.PreSendMsg;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.twilio.rest.api.v2010.account.Message;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MessageRecordServiceImpl implements MessageRecordService {
    
    @Autowired
    MessageRecordDao messageRecordDao;
    
    @Autowired
    JPAQueryFactory jpaQueryFactory;
    @Autowired
    ResourceBundle bundle;
    
    @Autowired
    Map<Integer,String> inboxStatusMap;
    @Autowired
    Map<Integer,String> outboxStatusMap;

    @Autowired
    private MobileNumberService mobileNumberService;

    @Autowired
    private ContactsService contactsService;

    @Autowired
    private KeywordService keywordService;

    @Autowired
    private SmsBillComponent smsBillComponent;

    @Autowired
    private SmsBillService smsBillService;

    @Autowired
    private TwilioUtil twilioUtil;
    
    @Override
    public int delete(List<Long> idList) {
        log.info("enter deleteOneMessage, param={}", idList);
        Assert.notNull(idList, CommonMessage.PARAM_IS_NULL);
        ServiceException.isTrue(idList.size() > 0,
                bundle.getString("msg-record-id-list").replace("$action$", "delete"));
        CustomerVo cur = Current.get();
        int result = 0;
        for (Long id : idList) {
            result += messageRecordDao.disableByIdAndCustomerId(id, cur.getId(), false);
        }
        log.info("leave deleteOneMessage");
        return result;
    }
    
    @Override
    public int markRead(List<Long> idList) {
        log.info("enter markRead, param={}", idList);
        Assert.notNull(idList, CommonMessage.PARAM_IS_NULL);
        ServiceException.isTrue(idList.size() > 0,
                bundle.getString("msg-record-id-list").replace("$action$", "mark as read"));
        CustomerVo cur = Current.get();
        int result = 0;
        for (Long id : idList) {
            result += messageRecordDao.markReadOne(id, cur.getId());
        }
        log.info("leave markRead");
        return result;
    }
    
    @Override
    public MessageRecord findById(Long id) {
        log.info("enter findById, id=" + id);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        MessageRecord messageRecord = messageRecordDao.findByIdAndCustomerIdAndDisableIsFalse(id, Current.get().getId());
        if (messageRecord != null && messageRecord.getInbox() && messageRecord.getStatus() == InboxStatus.UNREAD.getValue()) {
            messageRecordDao.markReadOne(messageRecord.getId());
        }
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
    
    @Override
    public Map<Integer, String> inboxStatusMap() {
        log.info("enter inboxStatusMap");
        log.info("leave inboxStatusMap");
        return inboxStatusMap;
    }
    
    @Override
    public Map<Integer, String> outboxStatusMap() {
        log.info("enter outboxStatusMap");
        log.info("leave outboxStatusMap");
        return outboxStatusMap;
    }

    @Override
    public void saveInbox(InboundMsg inboundMsg) {
        List<MobileNumber> list = mobileNumberService.findByNumberAndDisable(inboundMsg.getTo(), false);
        if (list.size()<=0){return;}
        MobileNumber mobileNumber = list.get(0);
        List<Contacts> contactsList = contactsService.findByPhoneAndCustomerId(inboundMsg.getFrom(), mobileNumber.getCustomerId());
        Contacts contacts = null;
        if (contactsList.size()<=0){
            contacts = new Contacts();
            contacts.setIsDelete(false);
            contacts.setInLock(false);
            contacts.setCustomerId(mobileNumber.getCustomerId());
            contacts.setPhone(inboundMsg.getFrom());
            contacts.setSource(ContactsSource.API_Added.getValue());
            contactsService.save(contacts);
        }else {
            contacts = contactsList.get(0);
        }
        MessageRecord messageRecord = new MessageRecord();
        messageRecord.setSegments(1);
        messageRecord.setSms((inboundMsg.getMediaList()!=null&&inboundMsg.getMediaList().size()>0)?false:true);
        messageRecord.setCustomerId(mobileNumber.getCustomerId());
        messageRecord.setCustomerNumber(inboundMsg.getTo());
        messageRecord.setContent(inboundMsg.getBody());
        List<String> urls = null;
        if (!messageRecord.getSms()){urls = inboundMsg.getMediaList().stream().map(s -> s.getMediaUrl()).collect(Collectors.toList());}
        messageRecord.setMediaList(urls!=null?urls.toString().substring(1,urls.toString().length()-1):null);
        messageRecord.setContactsId(contacts.getId());
        messageRecord.setContactsNumber(inboundMsg.getFrom());
        messageRecord.setInbox(true);
        messageRecord.setDisable(false);
        messageRecord.setSendTime(new Timestamp(System.currentTimeMillis()));
        messageRecord.setStatus(InboxStatus.UNREAD.getValue());
        messageRecord.setSid(inboundMsg.getMessageSid());
        messageRecord.setExpectedSendTime(new Timestamp(System.currentTimeMillis()));
        messageRecordDao.save(messageRecord);
        if (inboundMsg.getBody().indexOf(" ")!=-1){return;}
        List<Keyword> keywords = keywordService.findByCustomerIdAndTitle(mobileNumber.getCustomerId(), inboundMsg.getBody());
        if (keywords.size()<=0){return;}
        //自动回复
        MessageRecord send = new MessageRecord();
        send.setCustomerId(mobileNumber.getCustomerId());
        send.setCustomerNumber(inboundMsg.getTo());
        String content = keywords.get(0).getContent();
        content = content.replaceAll(MsgTemplateVariable.CON_FIRSTNAME.getTitle(), StringUtils.isEmpty(contacts.getFirstName())?"":contacts.getFirstName())
                .replaceAll(MsgTemplateVariable.CON_LASTNAME.getTitle(), StringUtils.isEmpty(contacts.getLastName())?"":contacts.getLastName());
        send.setContent(content);
        send.setSms(true);
        send.setContactsId(contacts.getId());
        send.setContactsNumber(inboundMsg.getFrom());
        send.setInbox(false);
        send.setDisable(false);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        send.setSendTime(timestamp);
        send.setExpectedSendTime(timestamp);
        send.setStatus(OutboxStatus.SENT.getValue());
        sendSms(send);
    }

    @Transactional
    public void sendSms(MessageRecord messageRecord){
        messageRecord.setSegments(MessageTools.calcMsgSegments(messageRecord.getContent()));
        Long sum = smsBillService.sumByCustomerId(messageRecord.getCustomerId());
        ServiceException.isTrue((sum==null?0l:sum)- messageRecord.getSegments()>=0,"Insufficient allowance");
        messageRecordDao.save(messageRecord);
        SmsBill smsBill = new SmsBill();
        smsBill.setAmount(-messageRecord.getSegments());
        smsBill.setCustomerId(messageRecord.getCustomerId());
        smsBill.setInfoDescribe("keyword automatic recovery");
        smsBillComponent.save(smsBill);
        PreSendMsg preSendMsg = new PreSendMsg();
        preSendMsg.setRecord(messageRecord);
        twilioUtil.sendMessage(preSendMsg);
    }
}
