package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingentity.MobileNumber;
import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessagePlanStatus;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.MsgTemplateVariable;
import com.adbest.smsmarketingentity.QMessagePlan;
import com.adbest.smsmarketingfront.dao.ContactsDao;
import com.adbest.smsmarketingfront.dao.MbNumberLibDao;
import com.adbest.smsmarketingfront.dao.MessagePlanDao;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingfront.entity.enums.RedisKey;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.MessagePlanService;
import com.adbest.smsmarketingfront.service.MmsBillComponent;
import com.adbest.smsmarketingfront.service.SmsBillComponent;
import com.adbest.smsmarketingfront.service.param.CreateMessagePlan;
import com.adbest.smsmarketingfront.service.param.GetMessagePlanPage;
import com.adbest.smsmarketingfront.service.param.UpdateMessagePlan;
import com.adbest.smsmarketingfront.util.CommonMessage;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.QueryDslTools;
import com.adbest.smsmarketingfront.util.TimeTools;
import com.adbest.smsmarketingfront.util.UrlTools;
import com.adbest.smsmarketingfront.util.twilio.MessageTools;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Service
@Slf4j
public class MessagePlanServiceImpl implements MessagePlanService {
    
    @Autowired
    MessagePlanDao messagePlanDao;
    @Autowired
    ContactsDao contactsDao;
    @Autowired
    MessageRecordDao messageRecordDao;
    @Autowired
    MbNumberLibDao mbNumberLibDao;
    
    @Autowired
    SmsBillComponent smsBillComponent;
    @Autowired
    MmsBillComponent mmsBillComponent;
    
    @Autowired
    JPAQueryFactory jpaQueryFactory;
    @Autowired
    ResourceBundle bundle;
    @Autowired
    RedisTemplate redisTemplate;
    
    @Value("${twilio.planExecTimeDelay}")
    private int planExecTimeDelay;
    
    @Autowired
    private Map<Integer,String> messagePlanStatusMap;
    
    @Transactional
    @Override
    public int create(CreateMessagePlan createPlan) {
        log.info("enter create, param={}", createPlan);
        // 参数检查
        checkMessagePlan(createPlan);
        // 检查客户有效号码
        List<String> fromNumList = validFromNumberLi(createPlan.getFromList());
        ServiceException.isTrue(fromNumList.size() > 0, bundle.getString("msg-plan-from-invalid"));
        // 消息定时任务入库，为下文提供id
        MessagePlan plan = new MessagePlan();
        createPlan.copy(plan);
        
        plan.setCustomerId(Current.get().getId());
        plan.setStatus(MessagePlanStatus.SCHEDULING.getValue());
        plan.setDisable(false);
        messagePlanDao.save(plan);
        // 消息入库
        int msgTotal = 0;
        if (createPlan.getToList() != null) {
            msgTotal += batchSaveMessage(createPlan, plan.getId());
        }
        if (createPlan.getGroupList() != null) {
            for (Long contactsGroupId : createPlan.getGroupList()) {
                msgTotal += batchSaveMessage(contactsGroupId, createPlan, plan.getId());
            }
        }
        // 产生消息账单
        if (createPlan.getMediaIdlList() == null || createPlan.getMediaIdlList().size() == 0) {
            smsBillComponent.saveSmsBill(bundle.getString("scheduled send: " + plan.getTitle()), -msgTotal);
        } else {
            mmsBillComponent.saveMmsBill(bundle.getString("scheduled send: " + plan.getTitle()), -msgTotal);
        }
        log.info("leave create");
        return 1;
    }
    
    @Transactional
    @Override
    public int update(UpdateMessagePlan update) {
        log.info("enter update, param={}", update);
        // 参数检查
        checkMessagePlan(update);
        // 检查任务
        Assert.notNull(update.getId(), CommonMessage.ID_CANNOT_EMPTY);
        CustomerVo cur = Current.get();
        MessagePlan found = messagePlanDao.findByIdAndCustomerIdAndDisableIsFalse(update.getId(), cur.getId());
        ServiceException.notNull(found, bundle.getString("msg-plan-not-exists"));
        // 检查客户有效号码
        List<String> fromNumList = validFromNumberLi(update.getFromList());
        ServiceException.isTrue(fromNumList.size() > 0, bundle.getString("msg-plan-from-invalid"));
        // 更新定时任务
        Assert.isTrue(Current.get().getId().equals(found.getCustomerId()), "Can only modify their own message plan.");
        ServiceException.isTrue(MessagePlanStatus.EDITING.getValue() == found.getStatus(),
                bundle.getString("msg-plan-status").replace("$action$", "update")
                        .replace("$status$", MessagePlanStatus.EDITING.getTitle())
        );
        update.copy(found);
        messagePlanDao.save(found);
        // 清除旧消息
        messageRecordDao.deleteByPlanId(found.getId());
        // 产生新消息
        int msgTotal = 0;
        if (update.getToList() != null) {
            msgTotal += batchSaveMessage(update, found.getId());
        }
        if (update.getGroupList() != null) {
            for (Long contactsGroupId : update.getGroupList()) {
                msgTotal += batchSaveMessage(contactsGroupId, update, found.getId());
            }
        }
        // TODO 校验套餐内余量
        
        log.info("leave update");
        return 1;
    }
    
    @Transactional
    @Override
    public int cancel(Long id) {
        log.info("enter cancel, id=" + id);
        // 参数校验
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        CustomerVo cur = Current.get();
        MessagePlan found = messagePlanDao.findByIdAndCustomerIdAndDisableIsFalse(id, cur.getId());
        ServiceException.notNull(found, bundle.getString("msg-plan-not-exists"));
        ServiceException.isTrue(MessagePlanStatus.SCHEDULING.getValue() == found.getStatus(),
                bundle.getString("msg-plan-status").replace("$action$", "cancel")
                        .replace("$status$", MessagePlanStatus.SCHEDULING.getTitle())
        );
        // 更新
        found.setStatus(MessagePlanStatus.EDITING.getValue());
        messagePlanDao.save(found);
        int msgTotal = messageRecordDao.sumMsgNumByPlanId(found.getId());
        // 返还消息条数
        List<String> urlList = UrlTools.getUrlList(found.getMediaIdList());
        if (urlList.size() > 0) {
            mmsBillComponent.saveMmsBill(bundle.getString("cancel scheduled send: " + found.getTitle()), msgTotal);
        } else {
            smsBillComponent.saveSmsBill(bundle.getString("cancel scheduled send: " + found.getTitle()), msgTotal);
        }
        log.info("leave cancel");
        return 1;
    }
    
    @Transactional
    @Override
    public int restart(Long id) {
        log.info("enter restart, id=" + id);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        CustomerVo cur = Current.get();
        MessagePlan found = messagePlanDao.findByIdAndCustomerIdAndDisableIsFalse(id, cur.getId());
        ServiceException.notNull(found, "msg-plan-not-exists");
        ServiceException.isTrue(MessagePlanStatus.EDITING.getValue() == found.getStatus(),
                bundle.getString("msg-plan-status").replace("$action$", "restart")
                        .replace("$status$", MessagePlanStatus.EDITING.getTitle())
        );
        ServiceException.isTrue(found.getExecTime().after(TimeTools.addMinutes(TimeTools.now(), planExecTimeDelay)),
                bundle.getString("msg-plan-execute-time-later").replace("$min$", Integer.toString(planExecTimeDelay)));
        // 更新
        found.setStatus(MessagePlanStatus.SCHEDULING.getValue());
        messagePlanDao.save(found);
        int msgTotal = messageRecordDao.sumMsgNumByPlanId(found.getId());
        // 扣除消息条数
        if (StringUtils.hasText(found.getMediaIdList())) {
            mmsBillComponent.saveMmsBill(bundle.getString("cancel scheduled send: " + found.getTitle()), -msgTotal);
        } else {
            smsBillComponent.saveSmsBill(bundle.getString("cancel scheduled send: " + found.getTitle()), -msgTotal);
        }
        log.info("leave restart");
        return 1;
    }
    
    @Override
    public MessagePlan findById(Long id) {
        log.info("enter findById, id=" + id);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        MessagePlan plan = messagePlanDao.findByIdAndCustomerIdAndDisableIsFalse(id, Current.get().getId());
        log.info("leave findById");
        return plan;
    }
    
    @Override
    public Page<MessagePlan> findByConditions(GetMessagePlanPage getPlanPage) {
        log.info("enter findByConditions, param={}", getPlanPage);
        Assert.notNull(getPlanPage, CommonMessage.PARAM_IS_NULL);
        QMessagePlan qMessagePlan = QMessagePlan.messagePlan;
        BooleanBuilder builder = new BooleanBuilder();
        QueryDslTools dslTools = new QueryDslTools(builder);
        dslTools.eqNotNull(qMessagePlan.status, getPlanPage.getStatus());
        dslTools.betweenNotNull(qMessagePlan.createTime, getPlanPage.getStart(), getPlanPage.getEnd());
        dslTools.containsNotEmpty(false, getPlanPage.getKeyword(), qMessagePlan.title);
        QueryResults<MessagePlan> queryResults = jpaQueryFactory.select(qMessagePlan).from(qMessagePlan)
                .where(builder)
                .offset(getPlanPage.getPage() * getPlanPage.getSize())
                .limit(getPlanPage.getSize())
                .fetchResults();
        Page<MessagePlan> planPage = PageBase.toPageEntity(queryResults, getPlanPage);
        log.info("leave findByConditions");
        return planPage;
    }
    
    @Override
    public Map<Integer, String> statusMap() {
        log.info("enter statusMap");
        log.info("leave statusMap");
        return messagePlanStatusMap;
    }
    
    private void checkMessagePlan(CreateMessagePlan create) {
        Assert.notNull(create, CommonMessage.PARAM_IS_NULL);
        
        ServiceException.hasText(create.getTitle(), bundle.getString("msg-plan-title"));
        
        ServiceException.notNull(create.getExecTime(), bundle.getString("msg-plan-execute-time"));
        ServiceException.isTrue(create.getExecTime().after(TimeTools.addMinutes(TimeTools.now(), planExecTimeDelay)),
                bundle.getString("msg-plan-execute-time-later").replace("$min$", Integer.toString(planExecTimeDelay)));
        
        ServiceException.hasText(create.getText(), bundle.getString("msg-plan-content"));
        
        ServiceException.isTrue(create.getMediaIdlList() == null || create.getMediaIdlList().size() <= MessageTools.MAX_MSG_MEDIA_NUM,
                bundle.getString("msg-plan-media-list"));
        
        ServiceException.isTrue(create.getFromNumList() != null && create.getFromNumList().size() > 0,
                bundle.getString("msg-plan-from"));
        
        ServiceException.isTrue((create.getToList() != null && create.getToList().size() > 0) ||
                        (create.getGroupList() != null && create.getGroupList().size() > 0),
                bundle.getString("msg-plan-contacts"));
    }
    
    private List<String> validFromNumberLi(List<Long> numberIdList) {
        Map<Long, String> validNumberMap = new HashMap<>();
        CustomerVo cur = Current.get();
        for (Long numberId : numberIdList) {
            MobileNumber mobileNumber = mbNumberLibDao.findByIdAndCustomerIdAndDisableIsFalse(numberId, cur.getId());
            if (mobileNumber != null) {
                validNumberMap.put(numberId, mobileNumber.getNumber());
            }
        }
        return (List<String>) validNumberMap.values();
    }
    
    private MessageRecord generateMessage(CreateMessagePlan plan, Contacts contacts) {
        CustomerVo cur = Current.get();
        // 计算实际消息内容
        String content = plan.getText()
                .replace(MsgTemplateVariable.CUS_FIRSTNAME.getTitle(), cur.getFirstName())
                .replace(MsgTemplateVariable.CUS_LASTNAME.getTitle(), cur.getLastName())
                .replace(MsgTemplateVariable.CON_FIRSTNAME.getTitle(), contacts.getFirstName())
                .replace(MsgTemplateVariable.CON_LASTNAME.getTitle(), contacts.getLastName());
        ServiceException.isTrue(MessageTools.isOverLength(content), MessageTools.isGsm7(content) ?
                bundle.getString("msg-plan-content-over-length-gsm7") : bundle.getString("msg-plan-content-over-length-ucs2"));
        MessageRecord messageRecord = new MessageRecord();
        // 根据消息类型判断是否需要分段发送
        if (plan.getMediaIdlList() == null || plan.getMediaIdlList().size() == 0) {
            messageRecord.setSegments(MessageTools.calcMsgSegments(content));
            messageRecord.setSms(true);
        } else {
            messageRecord.setSegments(1);
            messageRecord.setSms(false);
        }
        // 填充消息字段
        messageRecord.setCustomerId(cur.getId());
        messageRecord.setContent(content);
        messageRecord.setMediaList(UrlTools.getUrlsStr(plan.getMediaIdlList()));
        messageRecord.setContactsId(contacts.getId());
        messageRecord.setContactsNumber(contacts.getPhone());
        messageRecord.setInbox(false);
        messageRecord.setExpectedSendTime(plan.getExecTime());
        messageRecord.setStatus(OutboxStatus.PLANNING.getValue());
        messageRecord.setDisable(false);
        return messageRecord;
    }
    
    private int batchSaveMessage(CreateMessagePlan createPlan, Long planId) {
        List<Long> toList = createPlan.getToList();
        int msgNum = 0;  // 消息条数
        List<MessageRecord> msgTempList = new ArrayList<>();
        CustomerVo cur = Current.get();
        for (int i = 0; i < toList.size(); i++) {
            Long contactsId = toList.get(i);
            if (!uniqueValidForRecipient(planId, contactsId)) {
                continue;
            }
            // 联系人验证
            Contacts contacts = contactsDao.findByIdAndCustomerIdAndIsDeleteIsFalse(contactsId, cur.getId());
            if (contacts != null) {
                MessageRecord messageRecord = generateMessage(createPlan, contacts);
                messageRecord.setPlanId(planId);
                messageRecord.setCustomerNumber(createPlan.getFromNumList().get(i % createPlan.getFromNumList().size()));
                msgTempList.add(messageRecord);
                msgNum += messageRecord.getSegments();
            }
        }
        messageRecordDao.saveAll(msgTempList);
        return msgNum;
    }
    
    private int batchSaveMessage(Long contactsGroupId, CreateMessagePlan createPlan, Long planId) {
        int msgNum = 0;  // 消息条数
        int page = 0;
        Page<Contacts> contactsPage = null;
        do {
            contactsPage = contactsDao.findByContactsGroupId(contactsGroupId, PageRequest.of(page, 1000));
            List<Contacts> contactsList = contactsPage.getContent();
            List<MessageRecord> msgTempList = new ArrayList<>();
            for (int i = 0; i < contactsList.size(); i++) {
                Contacts contacts = contactsList.get(i);
                if (!uniqueValidForRecipient(planId, contacts.getId())) {
                    continue;
                }
                MessageRecord messageRecord = generateMessage(createPlan, contacts);
                messageRecord.setPlanId(planId);
                messageRecord.setCustomerNumber(createPlan.getFromNumList().get(i % createPlan.getFromNumList().size()));
                messageRecord.setContactsGroupId(contactsGroupId);
                msgTempList.add(messageRecord);
                msgNum += messageRecord.getSegments();
            }
            messageRecordDao.saveAll(msgTempList);
            page++;
        } while (contactsPage.hasNext());
        return msgNum;
    }
    
    // 接收消息号码去重
    private boolean uniqueValidForRecipient(Long planId, Long contactsId) {
        String key = new StringBuilder(RedisKey.TMP_PLAN_UNIQUE_CONTACTS.getKey()).append(planId).append(":").append(contactsId).toString();
        return redisTemplate.opsForValue().setIfAbsent(key, contactsId, RedisKey.TMP_PLAN_UNIQUE_CONTACTS.getExpireTime(), RedisKey.TMP_PLAN_UNIQUE_CONTACTS.getTimeUnit());
    }
    
}
