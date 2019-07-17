package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingentity.ContactsSource;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import com.adbest.smsmarketingfront.entity.vo.InboxMessageVo;
import com.adbest.smsmarketingfront.entity.vo.OutboxMessageVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.*;
import com.adbest.smsmarketingfront.service.param.GetInboxMessagePage;
import com.adbest.smsmarketingfront.service.param.GetOutboxMessagePage;
import com.adbest.smsmarketingfront.util.CommonMessage;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.twilio.MessageTools;
import com.adbest.smsmarketingfront.util.twilio.TwilioUtil;
import com.adbest.smsmarketingfront.util.twilio.param.InboundMsg;
import com.adbest.smsmarketingfront.util.twilio.param.PreSendMsg;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    Map<Integer, String> inboxStatusMap;
    @Autowired
    Map<Integer, String> outboxStatusMap;
    
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
    
    @Autowired
    private MessageRecordService messageRecordService;
    
    @Autowired
    private CustomerService customerService;
    
    @Value("${twilio.viewFileUrl}")
    private String viewFileUrl;

    @Autowired
    private MessageComponent messageComponent;
    
    @Override
    public int delete(List<Long> idList) {
        log.info("enter deleteOneMessage, param={}", idList);
        Assert.notNull(idList, CommonMessage.PARAM_IS_NULL);
        ServiceException.isTrue(idList.size() > 0,
                bundle.getString("msg-record-id-list").replace("$action$", bundle.getString("delete")));
        CustomerVo cur = Current.get();
        int result = 0;
        String errMsg = "";
        for (Long id : idList) {
            Optional<MessageRecord> optional = messageRecordDao.findById(id);
            if (optional.isPresent()) {
                MessageRecord message = optional.get();
                Assert.isTrue(cur.getId().equals(message.getCustomerId()), "can't delete other customer's message.");
                if (!message.getInbox() && OutboxStatus.QUEUE.getValue() == message.getStatus()) {
                    errMsg = bundle.getString("msg-record-delete-status")
                            .replace("$status$", outboxStatusMap.get(OutboxStatus.QUEUE.getValue()));
                    continue;
                }
                result += messageRecordDao.disableById(id, true);
            }
        }
        ServiceException.isTrue(StringUtils.isEmpty(errMsg), errMsg);
        log.info("leave deleteOneMessage");
        return result;
    }
    
    @Override
    public int markRead(List<Long> idList) {
        log.info("enter markRead, param={}", idList);
        Assert.notNull(idList, CommonMessage.PARAM_IS_NULL);
        ServiceException.isTrue(idList.size() > 0,
                bundle.getString("msg-record-id-list").replace("$action$", bundle.getString("mark-as-read")));
        CustomerVo cur = Current.get();
        int result = 0;
        for (Long id : idList) {
            result += messageRecordDao.updateStatusAfterReadMessage(id, cur.getId(), InboxStatus.ALREADY_READ.getValue());
        }
        log.info("leave markRead");
        return result;
    }
    
    @Override
    public InboxMessageVo findInboxMsgById(Long id) {
        log.info("enter findInboxMsgById, id={}", id);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        // TODO
        log.info("leave findInboxMsgById");
        return null;
    }
    
    @Override
    public OutboxMessageVo findOutboxMsgById(Long id) {
        log.info("enter findOutboxMsgById, id={}", id);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        MessageRecord messageRecord = messageRecordDao.findByIdAndCustomerIdAndDisableIsFalse(id, Current.get().getId());
        // TODO 标为已读
        log.info("leave findOutboxMsgById");
        return null;
    }
    
    @Override
    public Page<InboxMessageVo> findInboxByConditions(GetInboxMessagePage getInboxPage) {
        log.info("enter findInboxByConditions, param={}", getInboxPage);
        Assert.notNull(getInboxPage, CommonMessage.PARAM_IS_NULL);
        QMessageRecord qMessageRecord = QMessageRecord.messageRecord;
        QContacts qContacts = QContacts.contacts;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qMessageRecord.customerId.eq(Current.get().getId()));
        builder.and(qMessageRecord.disable.isFalse());
        builder.and(qMessageRecord.inbox.isTrue());
        getInboxPage.fillConditions(builder, qMessageRecord, qContacts);
        QueryResults<InboxMessageVo> queryResults = jpaQueryFactory.select(
                Projections.constructor(InboxMessageVo.class, qMessageRecord, qContacts.firstName, qContacts.lastName))
                .from(qMessageRecord)
                .leftJoin(qContacts).on(qMessageRecord.contactsId.eq(qContacts.id))
                .where(builder)
                .orderBy(qMessageRecord.status.asc(), qMessageRecord.createTime.desc())
                .offset(getInboxPage.getPage() * getInboxPage.getSize())
                .limit(getInboxPage.getSize())
                .fetchResults();
        Page<InboxMessageVo> messagePage = getInboxPage.toPageEntity(queryResults);
        log.info("leave findInboxByConditions");
        return messagePage;
    }
    
    @Override
    public Page<OutboxMessageVo> findOutboxByConditions(GetOutboxMessagePage getOutboxPage) {
        log.info("enter findOutboxByConditions, param={}", getOutboxPage);
        Assert.notNull(getOutboxPage, CommonMessage.PARAM_IS_NULL);
        QMessageRecord qMessageRecord = QMessageRecord.messageRecord;
        QContacts qContacts = QContacts.contacts;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qMessageRecord.customerId.eq(Current.get().getId()));
        builder.and(qMessageRecord.disable.isFalse());
        builder.and(qMessageRecord.inbox.isFalse());
        getOutboxPage.fillConditions(builder, qMessageRecord, qContacts);
        QueryResults<OutboxMessageVo> queryResults = jpaQueryFactory.select(
                Projections.constructor(OutboxMessageVo.class, qMessageRecord, qContacts.firstName, qContacts.lastName))
                .from(qMessageRecord)
                .leftJoin(qContacts).on(qMessageRecord.contactsId.eq(qContacts.id))
                .where(builder)
                .orderBy(qMessageRecord.createTime.desc())
                .offset(getOutboxPage.getPage() * getOutboxPage.getSize())
                .limit(getOutboxPage.getSize())
                .fetchResults();
        Page<OutboxMessageVo> messagePage = getOutboxPage.toPageEntity(queryResults);
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
        if (list.size() <= 0) {
            return;
        }
        MobileNumber mobileNumber = list.get(0);
        String from = inboundMsg.getFrom();
        from = from.startsWith("+1") ? from.substring(2, from.length()) : from;
        List<Contacts> contactsList = contactsService.findByPhoneAndCustomerId(from, mobileNumber.getCustomerId());
        Contacts contacts = null;
        if (contactsList.size() <= 0) {
            contacts = new Contacts();
            contacts.setIsDelete(false);
            contacts.setInLock(false);
            contacts.setCustomerId(mobileNumber.getCustomerId());
            contacts.setPhone(from);
            contacts.setSource(ContactsSource.API_Added.getValue());
            contactsService.save(contacts);
        } else {
            contacts = contactsList.get(0);
        }
        MessageRecord messageRecord = new MessageRecord();
        messageRecord.setSegments(1);
        messageRecord.setSms(inboundMsg.getMediaList() == null || inboundMsg.getMediaList().size() == 0);
        messageRecord.setCustomerId(mobileNumber.getCustomerId());
        messageRecord.setCustomerNumber(inboundMsg.getTo());
        messageRecord.setContent(inboundMsg.getBody());
        List<String> urls = null;
        if (!messageRecord.getSms()) {
            urls = inboundMsg.getMediaList().stream().map(s -> s.getMediaUrl()).collect(Collectors.toList());
        }
        messageRecord.setMediaList(urls != null ? urls.toString().substring(1, urls.toString().length() - 1) : null);
        messageRecord.setContactsId(contacts.getId());
        messageRecord.setContactsNumber(inboundMsg.getFrom());
        messageRecord.setInbox(true);
        messageRecord.setDisable(false);
        messageRecord.setSendTime(new Timestamp(System.currentTimeMillis()));
        messageRecord.setStatus(InboxStatus.UNREAD.getValue());
        messageRecord.setSid(inboundMsg.getMessageSid());
//        messageRecord.setExpectedSendTime(new Timestamp(System.currentTimeMillis()));
        messageRecordDao.save(messageRecord);
        if (inboundMsg.getBody().trim().indexOf(" ") != -1) {
            return;
        }
        List<Keyword> keywords = keywordService.findByCustomerIdAndTitle(mobileNumber.getCustomerId(), inboundMsg.getBody().trim());
        if (keywords.size() <= 0) {
            return;
        }
        
        MessageRecord send = new MessageRecord();
        send.setCustomerId(mobileNumber.getCustomerId());
        send.setCustomerNumber(inboundMsg.getTo());
        String content = keywords.get(0).getContent();
        Customer customer = customerService.findById(mobileNumber.getCustomerId());
        content = content.replaceAll(MsgTemplateVariable.CON_FIRSTNAME.getTitle(), StringUtils.isEmpty(contacts.getFirstName()) ? "" : contacts.getFirstName())
                .replaceAll(MsgTemplateVariable.CON_LASTNAME.getTitle(), StringUtils.isEmpty(contacts.getLastName()) ? "" : contacts.getLastName())
                .replaceAll(MsgTemplateVariable.CUS_FIRSTNAME.getTitle(), StringUtils.isEmpty(customer.getFirstName()) ? "" : customer.getFirstName())
                .replaceAll(MsgTemplateVariable.CUS_LASTNAME.getTitle(), StringUtils.isEmpty(customer.getLastName()) ? "" : customer.getLastName());
        send.setContent(content);
        send.setSms(true);
        send.setContactsId(contacts.getId());
        send.setContactsNumber(inboundMsg.getFrom());
        send.setInbox(false);
        send.setDisable(false);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        send.setSendTime(timestamp);
//        send.setExpectedSendTime(timestamp);
        send.setStatus(OutboxStatus.SENT.getValue());
        messageRecordService.sendSms(send, bundle.getString("KEYWORD_REPLY"));
    }
    
    @Override
    public void sendCallReminder(List<MessageRecord> messageRecords) {
        String msg = bundle.getString("CALL_RESPONSE");
        for (MessageRecord m : messageRecords) {
            messageRecordService.sendSms(m, msg);
        }
    }
    
    @Transactional
    public void sendSms(MessageRecord messageRecord, String msg) {
        messageRecord.setSegments(MessageTools.calcSmsSegments(messageRecord.getContent()));
        messageComponent.autoReplySettlement( messageRecord, "Automatic reply SMS");
        messageRecordDao.save(messageRecord);
        SmsBill smsBill = new SmsBill();
        smsBill.setAmount(-messageRecord.getSegments());
        smsBill.setCustomerId(messageRecord.getCustomerId());
        smsBill.setInfoDescribe(bundle.getString("KEYWORD_REPLY"));
        smsBillComponent.save(smsBill);
        PreSendMsg preSendMsg = new PreSendMsg(messageRecord);
//        twilioUtil.sendMessage(preSendMsg);
    }
}
