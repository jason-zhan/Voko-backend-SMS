package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingentity.ContactsSource;
import com.adbest.smsmarketingfront.entity.vo.*;
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
import com.twilio.rest.api.v2010.account.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
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
            if (!optional.isPresent()) {
                continue;
            }
            MessageRecord message = optional.get();
            if (message.getInbox() || message.getStatus() == OutboxStatus.DELIVERED.getValue() ||
                    message.getStatus() == OutboxStatus.UNDELIVERED.getValue() || message.getStatus() == OutboxStatus.FAILED.getValue()) {
                // 符合状态，禁用
                result += messageRecordDao.disableByIdAndCustomerId(id, cur.getId(), true);
            } else {
                errMsg = bundle.getString("msg-record-delete-status");
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
    public Page<InboxReport> findInboxReport(GetInboxMessagePage getInboxPage) {
        log.info("enter findInboxReport, param={}", getInboxPage);
        Assert.notNull(getInboxPage, CommonMessage.PARAM_IS_NULL);
        QMessageRecord qMessageRecord = QMessageRecord.messageRecord;
        QContacts qContacts = QContacts.contacts;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qMessageRecord.customerId.eq(Current.get().getId()));
        builder.and(qMessageRecord.disable.isFalse());
        builder.and(qMessageRecord.inbox.isTrue());
        getInboxPage.fillConditions(builder, qMessageRecord, qContacts);
        QueryResults<InboxReport> queryResults = jpaQueryFactory.select(
                Projections.constructor(InboxReport.class))
                .from(qMessageRecord)
                .where(builder)
                .orderBy(qMessageRecord.sendTime.desc())
                .fetchResults();
        Page<InboxReport> inReport = getInboxPage.toPageEntity(queryResults);
        log.info("leave findInboxReport");
        return inReport;
    }

    @Override
    public List<OutboxReport> findOutboxReport(GetOutboxMessagePage getOutboxPage) {
        log.info("enter findOutboxReport, param={}", getOutboxPage);
        String str = new SimpleDateFormat("yyyy-MM").format(getOutboxPage.getStart());
        List<OutboxReport> list = new ArrayList<>();
        for(int i = 1; i < 31; i++){
            String strKey = str ;
            if(i < 10) {
                strKey +=  "-0" + i;
            } else {
                strKey += "-" + i;
            }
            if(redisTemplate.hasKey(strKey)) {
                OutboxReport out = new OutboxReport(strKey, (long)redisTemplate.opsForValue().get(strKey));
                list.add(out);
            }
        }
        if(list.size() > 0) {
            return list;
        }

        Timestamp startT = getOutboxPage.getStart();
        Timestamp endT = getOutboxPage.getEnd();
        int page = 0;
        List<OutboxReport> outReport = messageRecordDao.findGroupBySendTimeAndCustomerId(startT,endT,Current.get().getId(), PageRequest.of(page, 100));

        for(int i = 0; i < outReport.size(); i++) {
            redisTemplate.opsForValue().set(outReport.get(i).getSendTime(), outReport.get(i).getCount());
        }

        log.info("leave findOutboxReport");
        log.info(String.valueOf(outReport));
        return outReport;
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
        send.setStatus(OutboxStatus.SENT.getValue());
        send.setReturnCode(MessageReturnCode.SENT.getValue());
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
        messageComponent.autoReplySettlement(messageRecord, msg);
        messageRecordDao.save(messageRecord);
        PreSendMsg preSendMsg = new PreSendMsg(messageRecord, viewFileUrl);
        Message message = twilioUtil.sendMessage(preSendMsg);
        messageRecord.setSid(message.getSid());
        messageRecordDao.save(messageRecord);
    }
    
    @Override
    public List<MessageRecord> findByReturnCodeAndDisableAndPlanIdIsNull(Integer returnCode, Boolean disable, Pageable pageable) {
        return messageRecordDao.findByReturnCodeAndDisableAndPlanIdIsNull(returnCode, disable, pageable);
    }
    
    @Override
    @Transactional
    public void saveAll(List<MessageRecord> successMsg) {
        messageRecordDao.saveAll(successMsg);
    }
    
    @Override
    @Transactional
    public void autoReplyReturn(MessageRecord messageRecord) {
        messageComponent.autoReplyReturn(messageRecord, bundle.getString("SMS_FAILED_RETURN"));
        messageRecordDao.save(messageRecord);
    }
}
