package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingentity.ContactsGroup;
import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingentity.CustomerMarketSetting;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingentity.MobileNumber;
import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessagePlanStatus;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.MsgTemplateVariable;
import com.adbest.smsmarketingentity.QMessagePlan;
import com.adbest.smsmarketingfront.dao.ContactsDao;
import com.adbest.smsmarketingfront.dao.ContactsGroupDao;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.dao.CustomerMarketSettingDao;
import com.adbest.smsmarketingfront.dao.MbNumberLibDao;
import com.adbest.smsmarketingfront.dao.MessagePlanDao;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingentity.ContactsSource;
import com.adbest.smsmarketingfront.entity.enums.RedisKey;
import com.adbest.smsmarketingfront.entity.middleware.MsgPlanState;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.MessageComponent;
import com.adbest.smsmarketingfront.service.MessagePlanService;
import com.adbest.smsmarketingfront.service.MmsBillComponent;
import com.adbest.smsmarketingfront.service.SmsBillComponent;
import com.adbest.smsmarketingfront.service.param.CreateMessagePlan;
import com.adbest.smsmarketingfront.service.param.GetMessagePlanPage;
import com.adbest.smsmarketingfront.service.param.UpdateMessagePlan;
import com.adbest.smsmarketingfront.task.plan.MessagePlanTask;
import com.adbest.smsmarketingfront.util.CommonMessage;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.EasyTime;
import com.adbest.smsmarketingfront.util.StrSegTools;
import com.adbest.smsmarketingfront.util.twilio.MessageTools;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

@Service
@Slf4j
public class MessagePlanServiceImpl implements MessagePlanService {
    
    @Autowired
    private MessagePlanDao messagePlanDao;
    @Autowired
    private ContactsDao contactsDao;
    @Autowired
    private ContactsGroupDao contactsGroupDao;
    @Autowired
    private MessageRecordDao messageRecordDao;
    @Autowired
    private MbNumberLibDao mbNumberLibDao;
    @Autowired
    private CustomerDao customerDao;
    @Autowired
    private CustomerMarketSettingDao customerMarketSettingDao;
    
    @Autowired
    private SmsBillComponent smsBillComponent;
    @Autowired
    private MmsBillComponent mmsBillComponent;
    @Autowired
    private MessageComponent messageComponent;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    @Autowired
    private ResourceBundle bundle;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MessagePlanTask messagePlanTask;
    
    @Value("${twilio.planExecTimeDelay}")
    private int planExecTimeDelay;
    @Value("${marketing.smsUnitPrice}")
    private BigDecimal smsUnitPrice;
    @Value("${marketing.mmsUnitPrice}")
    private BigDecimal mmsUnitPrice;
    
    @Value("${message.contacts.defaultFirstName}")
    private String contactsFirstName;
    @Value("${message.contacts.defaultLastName}")
    private String contactsLastName;
    
    @Autowired
    private Map<Integer, String> messagePlanStatusMap;
    
    @Transactional
    @Override
    public int create(CreateMessagePlan createPlan) {
        log.info("enter create, param={}", createPlan);
        // 参数检查
        checkMessagePlan(createPlan);
        // 校验用户消息余量
        validCustomerBalance(Current.get().getId(), createPlan.getMediaIdlList() == null || createPlan.getMediaIdlList().size() == 0);
        // 验证用户号码
        List<String> fromNumbers = checkFromNumbers(createPlan.getFromNumList());
        createPlan.setFromNumList(fromNumbers);
        // 保存任务，为下文提供信息
        MessagePlan plan = savePlan(createPlan);
        // 初始化中间参数实例
        MsgPlanState planState = MsgPlanState.init(plan.getId(), plan.getText(), createPlan.getMediaIdlList(), false);
        if (!planState.contactsVars) {
            // 如果不包含联系人变量，此时已经可以判定内容是否超长
            overLengthValid(planState.preContent);
        }
        // 计算消息总量
        toNumbersTraversal(createPlan, planState);
        contactsGroupsTraversal(createPlan, planState);
        // 根据消息量结算
        
        // 更新任务信用消费额
        
        log.info("leave create");
        return 1;
    }
    
    @Transactional
    @Override
    public int createInstant(CreateMessagePlan create) {
        log.info("enter createInstant, param={}", create);
        Assert.notNull(create, CommonMessage.PARAM_IS_NULL);
        // 设定执行时间
        create.setExecTime(EasyTime.now());
        // 持久化发送任务实体
        MessagePlan plan = createMessagePlan(create);
        // 分配任务、执行
        messagePlanTask.scheduledPlan(plan);
        log.info("leave createInstant");
        return 1;
    }
    
    @Transactional
    @Override
    public int update(UpdateMessagePlan update) {
        log.info("enter update, param={}", update);
        // 参数检查
        checkMessagePlan(update);
        ServiceException.isTrue(update.getExecTime().after(EasyTime.now()),
                bundle.getString("msg-plan-execute-time-later"));
        // 检查任务
        Assert.notNull(update.getId(), CommonMessage.ID_CANNOT_EMPTY);
        CustomerVo cur = Current.get();
        MessagePlan found = messagePlanDao.findByIdAndCustomerIdAndDisableIsFalse(update.getId(), cur.getId());
        ServiceException.notNull(found, bundle.getString("msg-plan-not-exists"));
        // 检查客户有效号码
        List<String> fromNumList = checkFromNumbers(update.getFromNumList());
        update.setFromNumList(fromNumList);
        // 更新定时任务
        Assert.isTrue(cur.getId().equals(found.getCustomerId()), "Can only modify their own message plan.");
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
        if (update.getToNumberList() != null) {
            msgTotal += batchSaveMessage(update, found.getId());
        }
        if (update.getGroupList() != null) {
            for (Long contactsGroupId : update.getGroupList()) {
                msgTotal += batchSaveMessage(contactsGroupId, update, found.getId());
            }
        }
        // 产生消息账单
        if (update.getMediaIdlList() == null || update.getMediaIdlList().size() == 0) {
            smsBillComponent.saveSmsBill(cur.getId(), "scheduled send: " + found.getTitle(), -msgTotal);
        } else {
            mmsBillComponent.saveMmsBill(cur.getId(), "scheduled send: " + found.getTitle(), -msgTotal);
        }
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
        List<String> urlList = StrSegTools.getStrList(found.getMediaIdList());
        if (urlList.size() > 0) {
            mmsBillComponent.saveMmsBill(cur.getId(), "cancel scheduled send: " + found.getTitle(), msgTotal);
        } else {
            smsBillComponent.saveSmsBill(cur.getId(), "cancel scheduled send: " + found.getTitle(), msgTotal);
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
        ServiceException.isTrue(found.getExecTime().after(EasyTime.now()),
                bundle.getString("msg-plan-execute-time-later"));
        // 更新
        found.setStatus(MessagePlanStatus.SCHEDULING.getValue());
        messagePlanDao.save(found);
        int msgTotal = messageRecordDao.sumMsgNumByPlanId(found.getId());
        // 扣除消息条数
        if (StringUtils.hasText(found.getMediaIdList())) {
            mmsBillComponent.saveMmsBill(cur.getId(), "restart scheduled send: " + found.getTitle(), -msgTotal);
        } else {
            smsBillComponent.saveSmsBill(cur.getId(), "restart scheduled send: " + found.getTitle(), -msgTotal);
        }
        log.info("leave restart");
        return 1;
    }
    
    @Override
    public int delete(Long id) {
        log.info("enter delete, id={}", id);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        Long curId = Current.get().getId();
        MessagePlan found = messagePlanDao.findByIdAndCustomerIdAndDisableIsFalse(id, curId);
        ServiceException.notNull(found, bundle.getString("msg-plan-not-exists"));
        int result = messagePlanDao.disableByIdAndCustomerId(id, curId, true);
        log.info("leave delete");
        return result;
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
        getPlanPage.fillConditions(builder, qMessagePlan);
        QueryResults<MessagePlan> queryResults = jpaQueryFactory.select(qMessagePlan).from(qMessagePlan)
                .where(builder)
                .offset(getPlanPage.getPage() * getPlanPage.getSize())
                .limit(getPlanPage.getSize())
                .fetchResults();
        Page<MessagePlan> planPage = getPlanPage.toPageEntity(queryResults);
        log.info("leave findByConditions");
        return planPage;
    }
    
    @Override
    public Map<Integer, String> statusMap() {
        log.info("enter statusMap");
        log.info("leave statusMap");
        return messagePlanStatusMap;
    }
    
    // 检查传入参数
    private void checkMessagePlan(CreateMessagePlan create) {
        ServiceException.hasText(create.getTitle(), bundle.getString("msg-plan-title"));
        
        ServiceException.hasText(create.getText(), bundle.getString("msg-plan-content"));
        ServiceException.isTrue(!MessageTools.isOverLength(MessageTools.trimTemplateVariables(create.getText())),
                MessageTools.isGsm7(create.getText()) ? bundle.getString("msg-content-over-length-gsm7") : bundle.getString("msg-content-over-length-ucs2"));
        
        ServiceException.notNull(create.getExecTime(), bundle.getString("msg-plan-execute-time"));
        ServiceException.isTrue(create.getExecTime().after(EasyTime.init().addMinutes(-5).stamp()), bundle.getString("msg-plan-execute-time-later"));
        
        ServiceException.isTrue(create.getMediaIdlList() == null || create.getMediaIdlList().size() <= MessageTools.MAX_MSG_MEDIA_NUM,
                bundle.getString("msg-plan-media-list"));
        // todo 验证资源
        
        ServiceException.isTrue(create.getFromNumList() != null && create.getFromNumList().size() > 0,
                bundle.getString("msg-plan-from"));
        
        ServiceException.isTrue((create.getToNumberList() != null && create.getToNumberList().size() > 0) ||
                        (create.getGroupList() != null && create.getGroupList().size() > 0),
                bundle.getString("msg-plan-contacts"));
    }
    
    // 检查用户状态
    private void validCustomerBalance(Long curId, boolean isSms) {
        CustomerMarketSetting marketSetting = customerMarketSettingDao.findFirstByCustomerId(curId);
        if (marketSetting != null) {
            if ((isSms && marketSetting.getSmsTotal() > 0) || (!isSms && marketSetting.getMmsTotal() > 0)) {
                return;
            }
        }
        Optional<Customer> optional = customerDao.findById(curId);
        Assert.isTrue(optional.isPresent(), "customer not exists");
        Customer customer = optional.get();
        ServiceException.isTrue(customer.getAvailableCredit().compareTo(isSms ? smsUnitPrice : mmsUnitPrice) >= 0,
                bundle.getString("credit-not-enough"));
    }
    
    // 持久化任务
    private MessagePlan savePlan(CreateMessagePlan create) {
        MessagePlan plan = new MessagePlan();
        create.copy(plan);
        CustomerVo cur = Current.get();
        plan.setCustomerId(cur.getId());
        plan.setDisable(false);
        plan.setStatus(MessagePlanStatus.SCHEDULING.getValue());
        return messagePlanDao.save(plan);
    }
    
    private List<String> checkFromNumbers(List<String> fromNumList) {
        Set<String> numberSet = new HashSet<>();
        Long curId = Current.get().getId();
        for (String number : fromNumList) {
            MobileNumber mobileNumber = mbNumberLibDao.findTopByCustomerIdAndNumberAndDisableIsFalse(curId, number);
            if (mobileNumber != null) {
                numberSet.add(mobileNumber.getNumber());
            }
        }
        ServiceException.isTrue(numberSet.size() > 0, bundle.getString("msg-plan-from-invalid"));
        return new ArrayList<>(numberSet);
    }
    
    private MessageRecord generateMessage(CreateMessagePlan plan, Contacts contacts) {
        CustomerVo cur = Current.get();
        // 计算实际消息内容
        String content = plan.getText()
                .replace(MsgTemplateVariable.CUS_FIRSTNAME.getTitle(), cur.getFirstName() == null ? "" : cur.getFirstName())
//                .replace(MsgTemplateVariable.CUS_FIRSTNAME.getTitle(), "01")
                .replace(MsgTemplateVariable.CUS_LASTNAME.getTitle(), cur.getLastName() == null ? "" : cur.getLastName())
//                .replace(MsgTemplateVariable.CUS_LASTNAME.getTitle(), "test")
                .replace(MsgTemplateVariable.CON_FIRSTNAME.getTitle(), contacts.getFirstName() == null ? "" : contacts.getFirstName())
                .replace(MsgTemplateVariable.CON_LASTNAME.getTitle(), contacts.getLastName() == null ? "" : contacts.getLastName());
        ServiceException.isTrue(MessageTools.isOverLength(content), MessageTools.isGsm7(content) ?
                bundle.getString("msg-content-over-length-gsm7") : bundle.getString("msg-content-over-length-ucs2"));
        MessageRecord messageRecord = new MessageRecord();
        // 根据消息类型判断是否需要分段发送
        if (plan.getMediaIdlList() == null || plan.getMediaIdlList().size() == 0) {
            messageRecord.setSegments(MessageTools.calcSmsSegments(content));
            messageRecord.setSms(true);
        } else {
            messageRecord.setSegments(1);
            messageRecord.setSms(false);
        }
        // 填充消息字段
        messageRecord.setCustomerId(cur.getId());
//        messageRecord.setCustomerId(1L);
        messageRecord.setContent(content);
        messageRecord.setMediaList(StrSegTools.getListStr(plan.getMediaIdlList()));
        messageRecord.setContactsId(contacts.getId());
        messageRecord.setContactsNumber(contacts.getPhone());
        messageRecord.setInbox(false);
//        messageRecord.setExpectedSendTime(plan.getExecTime());
        messageRecord.setStatus(OutboxStatus.PLANNING.getValue());
        messageRecord.setDisable(false);
        return messageRecord;
    }
    
    private int batchSaveMessage(CreateMessagePlan createPlan, Long planId) {
        // 创建联系人
        List<Contacts> contactsList = batchSaveContacts(createPlan.getToNumberList());
        int msgNum = 0;  // 消息条数
        List<MessageRecord> msgTempList = new ArrayList<>();
        for (int i = 0; i < contactsList.size(); i++) {
            Contacts contacts = contactsList.get(i);
            if (isRepeatRecipient(planId, contacts.getId())) {
                continue;
            }
            // 联系人验证
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
        // 组验证
        ContactsGroup contactsGroup = contactsGroupDao.findByIdAndCustomerId(contactsGroupId, Current.get().getId());
//        ContactsGroup contactsGroup = contactsGroupDao.findByIdAndCustomerId(contactsGroupId, 1L);
        if (contactsGroup == null) {
            log.info("contacts group(%s) not exists!", contactsGroupId);
            return 0;
        }
        int msgNum = 0;  // 消息条数
        int page = 0;
        Page<Contacts> contactsPage = null;
        do {
            contactsPage = contactsDao.findUsableByGroupId(contactsGroupId, PageRequest.of(page, 1000));
            List<Contacts> contactsList = contactsPage.getContent();
            List<MessageRecord> msgTempList = new ArrayList<>();
            for (int i = 0; i < contactsList.size(); i++) {
                Contacts contacts = contactsList.get(i);
                if (isRepeatRecipient(planId, contacts.getId())) {
                    continue;
                }
                MessageRecord messageRecord = generateMessage(createPlan, contacts);
                messageRecord.setPlanId(planId);
                messageRecord.setCustomerNumber(createPlan.getFromNumList().get(i % createPlan.getFromNumList().size()));
//                messageRecord.setContactsGroupId(contactsGroupId);
                msgTempList.add(messageRecord);
                msgNum += messageRecord.getSegments();
            }
            messageRecordDao.saveAll(msgTempList);
            page++;
        } while (contactsPage.hasNext());
        return msgNum;
    }
    
    /**
     * 验证接收消息号码是否重复
     *
     * @param planId
     * @param contactsId
     * @return true:repeat | false:absent
     */
    private boolean isRepeatRecipient(Long planId, Long contactsId) {
        String key = new StringBuilder(RedisKey.TMP_PLAN_UNIQUE_CONTACTS.getKey()).append(planId).append(":").append(contactsId).toString();
        return !redisTemplate.opsForValue().setIfAbsent(key, contactsId, RedisKey.TMP_PLAN_UNIQUE_CONTACTS.getExpireTime(), RedisKey.TMP_PLAN_UNIQUE_CONTACTS.getTimeUnit());
    }
    
    // 批量创建联系人
    private List<Contacts> batchSaveContacts(List<String> numberList) {
        List<Contacts> contactsList = new ArrayList<>();
        if (numberList == null || numberList.size() == 0) {
            return contactsList;
        }
        List<Contacts> newContactsList = new ArrayList<>();
        Long curId = Current.get().getId();
        for (String number : numberList) {
            List<Contacts> foundList = contactsDao.findByPhoneAndCustomerId(number, curId);
            if (foundList.size() > 0) {
                contactsList.add(foundList.get(0));
                continue;
            }
            Contacts contacts = new Contacts();
            contacts.setPhone(number);
            contacts.setSource(ContactsSource.API_Added.getValue());
            contacts.setCustomerId(curId);
            contacts.setInLock(false);
            contacts.setIsDelete(false);
            newContactsList.add(contacts);
        }
        if (newContactsList.size() > 0) {
            contactsList.addAll(contactsDao.saveAll(newContactsList));
        }
        return contactsList;
    }
    
    private MessagePlan createMessagePlan(CreateMessagePlan createPlan) {
        Assert.notNull(createPlan, CommonMessage.PARAM_IS_NULL);
        // 参数检查
        checkMessagePlan(createPlan);
        ServiceException.isTrue(createPlan.getExecTime().after(EasyTime.init().addSeconds(-10).stamp()),
                bundle.getString("msg-plan-execute-time-later"));
        // 检查客户有效号码
        List<String> fromNumList = checkFromNumbers(createPlan.getFromNumList());
        createPlan.setFromNumList(fromNumList);
        // 消息定时任务入库，为下文提供id
        MessagePlan plan = new MessagePlan();
        createPlan.copy(plan);
        CustomerVo cur = Current.get();
        plan.setCustomerId(cur.getId());
        plan.setStatus(MessagePlanStatus.SCHEDULING.getValue());
        plan.setDisable(false);
        messagePlanDao.save(plan);
        // 消息入库
        int msgTotal = 0;
        if (createPlan.getToNumberList() != null) {
            msgTotal += batchSaveMessage(createPlan, plan.getId());
        }
        if (createPlan.getGroupList() != null) {
            for (Long contactsGroupId : createPlan.getGroupList()) {
                msgTotal += batchSaveMessage(contactsGroupId, createPlan, plan.getId());
            }
        }
        ServiceException.isTrue(msgTotal > 0, bundle.getString("msg-plan-contacts"));
        // 产生消息账单
        if (createPlan.getMediaIdlList() == null || createPlan.getMediaIdlList().size() == 0) {
            smsBillComponent.saveSmsBill(cur.getId(), "scheduled send: " + plan.getTitle(), -msgTotal);
        } else {
            mmsBillComponent.saveMmsBill(cur.getId(), "scheduled send: " + plan.getTitle(), -msgTotal);
        }
        return plan;
    }
    
    // 遍历输入的手机号并计算消息量
    private void toNumbersTraversal(CreateMessagePlan createPlan, MsgPlanState planState) {
        if (createPlan.getToNumberList() == null || createPlan.getToNumberList().size() == 0) {
            return;
        }
        int fromListSize = createPlan.getFromNumList().size();
        List<MessageRecord> messageList = new ArrayList<>();
        List<Contacts> newContactsList = new ArrayList<>();
        for (String number : createPlan.getToNumberList()) {
            // 存在性验证
            Contacts contacts = contactsDao.findFirstByCustomerIdAndPhone(planState.cur.getId(), number);
            if (contacts == null) {
                // 不存在，生成联系人并加入新建联系人列表
                contacts = generateContacts(planState.cur.getId(), number);
                newContactsList.add(contacts);
                continue;
            }
            calcMsg(messageList, createPlan, planState, contacts, fromListSize);
        }
        // 批量持久化增加的联系人并计算消息数
        if (newContactsList.size() > 0) {
            contactsDao.saveAll(newContactsList);
            String content = "";
            if (planState.contactsVars) {
                // 默认联系人名称和姓氏
                // TODO 内容优化
                content = MessageTools.replaceContactsVariables(planState.preContent, contactsFirstName, contactsLastName);
                overLengthValid(content);
            }
            // 计算分段数
            int segments = planState.isSms ? (planState.contactsVars ? MessageTools.calcSmsSegments(content) : planState.preSegments) : 1;
            planState.msgTotal += segments * newContactsList.size();
            if (planState.saveMsg) {
                for (Contacts contacts : newContactsList) {
                    // 新增的号码，跳过各项验证
                    messageList.add(generateMessage(
                            planState,
                            contacts,
                            createPlan.getFromNumList().get(planState.counter % fromListSize),
                            content,
                            segments
                    ));
                    planState.counter++;
                }
            }
        }
        // 批量保存消息
        if (messageList.size() > 0) {
            messageRecordDao.saveAll(messageList);
        }
    }
    
    // 生成联系人实例
    private Contacts generateContacts(Long customerId, String phone) {
        Contacts contacts = new Contacts();
        contacts.setCustomerId(customerId);
        contacts.setPhone(phone);
        contacts.setInLock(false);
        contacts.setIsDelete(false);
        contacts.setSource(ContactsSource.API_Added.getValue());
        return contacts;
    }
    
    // 生成消息实例
    private MessageRecord generateMessage(MsgPlanState planState, Contacts contacts, String fromNumber, String content, int segments) {
        MessageRecord message = new MessageRecord();
        message.setPlanId(planState.planId);
        message.setCustomerId(planState.cur.getId());
        message.setSms(planState.isSms);
        message.setInbox(false);
        message.setStatus(OutboxStatus.PLANNING.getValue());
        message.setDisable(false);
        message.setCustomerNumber(fromNumber);
        message.setContent(content);
        message.setSegments(segments);
        message.setMediaList(planState.mediaListStr);
        message.setContactsId(contacts.getId());
        message.setContactsNumber(contacts.getPhone());
        return message;
    }
    
    
    // 遍历所有群组并计算消息量
    private void contactsGroupsTraversal(CreateMessagePlan createPlan, MsgPlanState planState) {
        if (createPlan.getGroupList() == null || createPlan.getGroupList().size() == 0) {
            return;
        }
        for (Long groupId : createPlan.getGroupList()) {
            groupTraversal(groupId, createPlan, planState);
        }
    }
    
    // 遍历群组所有联系人并计算生成的消息数
    private void groupTraversal(Long groupId, CreateMessagePlan createPlan, MsgPlanState planState) {
        Page<Contacts> contactsPage = null;
        Pageable pageable = PageRequest.of(0, 1000);
        int fromListSize = createPlan.getFromNumList().size();
        do {
            contactsPage = contactsDao.findUsableByCustomerIdAndGroupId(planState.cur.getId(), groupId, pageable);
            // 群组验证
            if (contactsPage.isEmpty()) {
                break;
            }
            List<MessageRecord> messageList = new ArrayList<>();
            for (Contacts contacts : contactsPage.getContent()) {
                calcMsg(messageList, createPlan, planState, contacts, fromListSize);
            }
            // 批量保存消息
            if (messageList.size() > 0) {
                messageRecordDao.saveAll(messageList);
            }
            pageable = pageable.next();
        } while (contactsPage.hasNext());
    }
    
    private void calcMsg(List<MessageRecord> messageList, CreateMessagePlan createPlan, MsgPlanState planState, Contacts contacts, int fromListSize) {
        //  合法性验证
        if (contacts.getIsDelete() || contacts.getInLock()) {
            return;
        }
        //  唯一性验证
        if (isRepeatRecipient(planState.planId, contacts.getId())) {
            return;
        }
        //  计算内容
        String content = "";
        if (planState.contactsVars) {
            content = MessageTools.replaceContactsVariables(planState.preContent, contacts.getFirstName(), contacts.getLastName());
            overLengthValid(content);
        }
        //  计算分段数
        int segments = planState.isSms ? (planState.contactsVars ? MessageTools.calcSmsSegments(content) : planState.preSegments) : 1;
        planState.msgTotal += segments;
        //  持久化消息
        if (planState.saveMsg) {
            messageList.add(generateMessage(
                    planState,
                    contacts,
                    createPlan.getFromNumList().get(planState.counter % fromListSize),
                    content,
                    segments
            ));
            planState.counter++;
        }
    }
    
    // 计算消息数：内容不包含模板变量
    private int calcMsgNum(MessagePlan plan, List<Contacts> contactsList) {
        // 内容超长验证
        ServiceException.isTrue(MessageTools.isOverLength(plan.getText()), MessageTools.isGsm7(plan.getText()) ?
                bundle.getString("msg-content-over-length-gsm7") : bundle.getString("msg-content-over-length-ucs2"));
        int contactsNum = 0;
        for (Contacts contacts : contactsList) {
            // 验证是否重复号码
            if (isRepeatRecipient(plan.getId(), contacts.getId())) {
                continue;
            }
            contactsNum++;
        }
        boolean isSms = StrSegTools.getStrList(plan.getMediaIdList()).size() == 0;
        if (isSms) {
            return MessageTools.calcSmsSegments(plan.getText()) * contactsNum;
        } else {
            return contactsNum;
        }
    }
    
    // 计算消息数：内容包含模板变量
    private int calcMsgNum(MessagePlan plan, List<Contacts> contactsList, CustomerVo cur) {
        int msgNum = 0;
        boolean isSms = StrSegTools.getStrList(plan.getMediaIdList()).size() == 0;
        for (Contacts contacts : contactsList) {
            // 验证是否重复号码
            if (isRepeatRecipient(plan.getId(), contacts.getId())) {
                continue;
            }
            // 计算内容
            String content = plan.getText()
                    .replace(MsgTemplateVariable.CUS_FIRSTNAME.getTitle(), cur.getFirstName() == null ? "" : cur.getFirstName())
                    .replace(MsgTemplateVariable.CUS_LASTNAME.getTitle(), cur.getLastName() == null ? "" : cur.getLastName())
                    .replace(MsgTemplateVariable.CON_FIRSTNAME.getTitle(), contacts.getFirstName() == null ? "" : contacts.getFirstName())
                    .replace(MsgTemplateVariable.CON_LASTNAME.getTitle(), contacts.getLastName() == null ? "" : contacts.getLastName());
            ServiceException.isTrue(MessageTools.isOverLength(content), MessageTools.isGsm7(content) ?
                    bundle.getString("msg-content-over-length-gsm7") : bundle.getString("msg-content-over-length-ucs2"));
            // 根据短信或彩信计算消息条数
            if (isSms) {
                msgNum += MessageTools.calcSmsSegments(content);
            } else {
                msgNum++;
            }
        }
        return msgNum;
    }
    
    // 消息结算
    
    // 内容超长验证提示
    private void overLengthValid(String content) {
        ServiceException.isTrue(MessageTools.isOverLength(content), MessageTools.isGsm7(content) ?
                bundle.getString("msg-content-over-length-gsm7") : bundle.getString("msg-content-over-length-ucs2"));
    }
}
