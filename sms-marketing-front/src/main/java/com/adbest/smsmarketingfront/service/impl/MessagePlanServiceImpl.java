package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.ContactsGroup;
import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingentity.CustomerMarketSetting;
import com.adbest.smsmarketingentity.MobileNumber;
import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessagePlanStatus;
import com.adbest.smsmarketingentity.QMessagePlan;
import com.adbest.smsmarketingfront.dao.ContactsDao;
import com.adbest.smsmarketingfront.dao.ContactsGroupDao;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.dao.CustomerMarketSettingDao;
import com.adbest.smsmarketingfront.dao.MbNumberLibDao;
import com.adbest.smsmarketingfront.dao.MessagePlanDao;
import com.adbest.smsmarketingfront.entity.middleware.MsgPlanState;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import com.adbest.smsmarketingfront.entity.vo.MessagePlanVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.CreditBillComponent;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MessagePlanServiceImpl implements MessagePlanService {
    
    @Autowired
    private MessagePlanDao messagePlanDao;
    @Autowired
    private MbNumberLibDao mbNumberLibDao;
    @Autowired
    private CustomerDao customerDao;
    @Autowired
    private CustomerMarketSettingDao customerMarketSettingDao;
    @Autowired
    ContactsDao contactsDao;
    @Autowired
    private ContactsGroupDao contactsGroupDao;
    
    @Autowired
    private SmsBillComponent smsBillComponent;
    @Autowired
    private MmsBillComponent mmsBillComponent;
    @Autowired
    private MessageComponent messageComponent;
    @Autowired
    private CreditBillComponent creditBillComponent;
    @Autowired
    private MessagePlanTask messagePlanTask;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    @Autowired
    private ResourceBundle bundle;
    
    @Value("${twilio.planExecTimeDelay}")
    private int planExecTimeDelay;
    
    @Autowired
    private Map<Integer, String> messagePlanStatusMap;
    
    @Transactional
    @Override
    public int create(CreateMessagePlan createPlan) {
        log.info("enter create, param={}", createPlan);
        Assert.notNull(createPlan, CommonMessage.PARAM_IS_NULL);
        CustomerVo cur = Current.get();
        // 参数校验(包括各列表属性去重)
        checkMsgPlanInfo(createPlan);
        // 检查客户号码(发送消息的号码)
        createPlan.setFromNumList(checkFromNumbers(createPlan.getFromNumList()));
        // 验证用户消息余量
        validCustomerBalance(cur.getId(), createPlan.isSms(), createPlan.getToNumberList(), createPlan.getGroupList());
        // 持久化任务(消息结算需要任务id)
        boolean closeToExecTime = closeToExecTime(createPlan.getExecTime());
        MessagePlan plan = generatePlan(createPlan, closeToExecTime);
        messagePlanDao.save(plan);
        // 初始化中间参数实例
        MsgPlanState planState = MsgPlanState.init(plan, createPlan, cur, closeToExecTime);
        // 消息结算
        messageComponent.msgPlanSettlement(planState);
        // 修改任务结算信息
        plan.setMsgTotal(planState.msgTotal);
        plan.setCreditPayNum(planState.creditPayNum);
        plan.setCreditPayCost(planState.creditPayCost);
        // 生成信用账单
        if (planState.creditPayCost.compareTo(BigDecimal.ZERO) > 0) {  // 若有信用额度支付，必大于0
            creditBillComponent.savePlanConsume(plan.getCustomerId(), plan.getId(), planState.creditPayCost.negate(), bundle.getString("bill-create-plan"));
        }
        // 产生消息账单
        saveMsgBill(cur.getId(), plan.getIsSms(), -plan.getMsgTotal(), bundle.getString("bill-create-plan"));
        // 若临近执行时间，则将任务加入容器
        if (closeToExecTime) {
            messagePlanTask.scheduledPlan(plan);
        }
        log.info("leave create");
        return 1;
    }
    
    @Transactional
    @Override
    public int createInstant(CreateMessagePlan create) {
        log.info("enter createInstant, param={}", create);
        Assert.notNull(create, CommonMessage.PARAM_IS_NULL);
        CustomerVo cur = Current.get();
        // 设定执行时间
        create.setExecTime(EasyTime.now());
        // 参数校验(包括各列表属性去重)
        checkMsgPlanInfo(create);
        // 检查客户号码(发送消息的号码)
        create.setFromNumList(checkFromNumbers(create.getFromNumList()));
        // 验证用户消息余量
        validCustomerBalance(cur.getId(), create.isSms(), create.getToNumberList(), create.getGroupList());
        // 生成任务实例
        MessagePlan plan = generatePlan(create, true);
        // 持久化任务(消息结算需要任务id)
        messagePlanDao.save(plan);
        // 初始化中间参数实例
        MsgPlanState planState = MsgPlanState.init(plan, create, cur, true);
        // 消息结算
        messageComponent.msgPlanSettlement(planState);
        // 修改任务结算信息
        plan.setMsgTotal(planState.msgTotal);
        plan.setCreditPayNum(planState.creditPayNum);
        plan.setCreditPayCost(planState.creditPayCost);
        // 生成信用账单
        if (planState.creditPayCost.compareTo(BigDecimal.ZERO) > 0) {  // 若有信用额度支付，必大于0
            creditBillComponent.savePlanConsume(plan.getCustomerId(), plan.getId(), planState.creditPayCost.negate(), bundle.getString("bill-create-plan"));
        }
        // 产生消息账单
        saveMsgBill(cur.getId(), plan.getIsSms(), -plan.getMsgTotal(), bundle.getString("bill-create-plan"));
        // 将任务加入schedule容器等待执行
        messagePlanTask.scheduledPlan(plan);
        log.info("leave createInstant");
        return 1;
    }
    
    @Transactional
    @Override
    public int update(UpdateMessagePlan update) {
        log.info("enter update, param={}", update);
        Assert.notNull(update, CommonMessage.PARAM_IS_NULL);
        // 参数检查
        checkMsgPlanInfo(update);
        CustomerVo cur = Current.get();
        // 检查任务
        Assert.notNull(update.getId(), CommonMessage.ID_CANNOT_EMPTY);
        MessagePlan found = messagePlanDao.findByIdAndCustomerIdAndDisableIsFalse(update.getId(), cur.getId());
        ServiceException.notNull(found, bundle.getString("msg-plan-not-exists"));
        ServiceException.isTrue(MessagePlanStatus.EDITING.getValue() == found.getStatus(),
                bundle.getString("msg-plan-status").replace("$action$", bundle.getString("update"))
                        .replace("$status$", messagePlanStatusMap.get(MessagePlanStatus.EDITING.getValue()))
        );
        // 检查客户号码(发送消息的号码)
        update.setFromNumList(checkFromNumbers(update.getFromNumList()));
        // 更新任务
        update.copy(found);
        messagePlanDao.save(found);
        log.info("leave update");
        return 1;
    }
    
    @Override
    public boolean checkCrossBeforeCancel(Long id) {
        log.info("enter checkCrossBeforeCancel, id={}", id);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        // 检查任务
        MessagePlan found = messagePlanDao.findByIdAndCustomerIdAndDisableIsFalse(id, Current.get().getId());
        ServiceException.notNull(found, bundle.getString("msg-plan-not-exists"));
        ServiceException.isTrue(MessagePlanStatus.SCHEDULING.getValue() == found.getStatus(),
                bundle.getString("msg-plan-status").replace("$action$", bundle.getString("cancel"))
                        .replace("$status$", messagePlanStatusMap.get(MessagePlanStatus.SCHEDULING.getValue()))
        );
        boolean isCross = crossedPackages(found);
        log.info("leave checkCrossBeforeCancel");
        return !isCross;
    }
    
    @Transactional
    @Override
    public int cancel(Long id) {
        log.info("enter cancel, id=" + id);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        CustomerVo cur = Current.get();
        // 检查任务
        MessagePlan found = messagePlanDao.findByIdAndCustomerIdAndDisableIsFalse(id, cur.getId());
        ServiceException.notNull(found, bundle.getString("msg-plan-not-exists"));
        ServiceException.isTrue(MessagePlanStatus.SCHEDULING.getValue() == found.getStatus(),
                bundle.getString("msg-plan-status").replace("$action$", bundle.getString("cancel"))
                        .replace("$status$", messagePlanStatusMap.get(MessagePlanStatus.SCHEDULING.getValue()))
        );
        // 套餐周期内，返还套餐量
        int rebateMsg = found.getMsgTotal() - found.getCreditPayNum();
        if (!crossedPackages(found) && rebateMsg > 0) {
            if (found.getIsSms()) {
                customerMarketSettingDao.updateSmsByCustomerId(cur.getId(), rebateMsg);
            } else {
                customerMarketSettingDao.updateMmsByCustomerId(cur.getId(), rebateMsg);
            }
        }
        // 无论是否套餐周期内，都须返还信用消费
        if (found.getCreditPayCost().compareTo(BigDecimal.ZERO) > 0) {
            customerDao.paymentByCredit(cur.getId(), found.getCreditPayCost());
            creditBillComponent.savePlanConsume(cur.getId(), found.getId(), found.getCreditPayCost(), bundle.getString("bill-cancel-plan"));
        }
        saveMsgBill(cur.getId(), found.getIsSms(), found.getMsgTotal(), bundle.getString("bill-cancel-plan"));
        // 更新任务 - 变更为编辑中
        int cancelResult = messagePlanDao.cancelMessagePlan(found.getId(), MessagePlanStatus.EDITING.getValue(), MessagePlanStatus.SCHEDULING.getValue());
        ServiceException.isTrue(cancelResult > 0, bundle.getString("msg-plan-cancel-failed"));
        log.info("leave cancel");
        return 1;
    }
    
    @Transactional
    @Override
    public int restart(Long id) {
        log.info("enter restart, id=" + id);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        CustomerVo cur = Current.get();
        // 检查任务
        MessagePlan found = messagePlanDao.findByIdAndCustomerIdAndDisableIsFalse(id, cur.getId());
        ServiceException.notNull(found, "msg-plan-not-exists");
        ServiceException.isTrue(MessagePlanStatus.EDITING.getValue() == found.getStatus(),
                bundle.getString("msg-plan-status").replace("$action$", bundle.getString("restart"))
                        .replace("$status$", messagePlanStatusMap.get(MessagePlanStatus.EDITING.getValue()))
        );
        ServiceException.isTrue(found.getExecTime().after(curTime()),
                bundle.getString("msg-plan-execute-time-later"));
        // 验证用户消息余量
        validCustomerBalance(cur.getId(), found.getIsSms(), StrSegTools.getStrList(found.getToNumList()), StrSegTools.getLongList(found.getToGroupList()));
        // 初始化中间参数实例
        boolean closeToExecTime = closeToExecTime(found.getExecTime());
        MsgPlanState planState = MsgPlanState.init(found, cur, closeToExecTime);
        // 消息结算
        messageComponent.msgPlanSettlement(planState);
        // 修改任务结算信息
        found.setStatus(closeToExecTime ? MessagePlanStatus.QUEUING.getValue() : MessagePlanStatus.SCHEDULING.getValue());
        found.setMsgTotal(planState.msgTotal);
        found.setCreditPayNum(planState.creditPayNum);
        found.setCreditPayCost(planState.creditPayCost);
        // 更新任务
        messagePlanDao.save(found);
        // 生成信用账单
        if (planState.creditPayCost.compareTo(BigDecimal.ZERO) > 0) {  // 若有信用额度支付，必大于0
            creditBillComponent.savePlanConsume(cur.getId(), found.getId(), planState.creditPayCost.negate(), bundle.getString("bill-restart-plan"));
        }
        // 产生消息账单
        saveMsgBill(cur.getId(), found.getIsSms(), -found.getMsgTotal(), bundle.getString("bill-restart-plan"));
        // 若临近执行时间，则将任务加入容器
        if (closeToExecTime) {
            messagePlanTask.scheduledPlan(found);
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
        Assert.state(MessagePlanStatus.EDITING.getValue() == found.getStatus(),
                bundle.getString("msg-plan-status").replace("$action$", bundle.getString("delete"))
                        .replace("$status$", messagePlanStatusMap.get(MessagePlanStatus.EDITING.getValue()))
        );
        int result = messagePlanDao.disableByIdAndCustomerId(id, curId, true);
        log.info("leave delete");
        return result;
    }
    
    @Override
    public MessagePlanVo findById(Long id) {
        log.info("enter findById, id=" + id);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        Long curId = Current.get().getId();
        MessagePlan plan = messagePlanDao.findByIdAndCustomerIdAndDisableIsFalse(id, curId);
        if (plan == null) {
            return null;
        }
        List<ContactsGroup> groupList = contactsGroupDao.findByCustomerIdAndIdIn(curId, StrSegTools.getLongList(plan.getToGroupList()));
        MessagePlanVo planVo = new MessagePlanVo(plan, groupList);
        log.info("leave findById");
        return planVo;
    }
    
    @Override
    public Page<MessagePlanVo> findByConditions(GetMessagePlanPage getPlanPage) {
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
        List<MessagePlanVo> planVoList = queryResults.getResults().stream().map(plan -> new MessagePlanVo(plan, new ArrayList<>())).collect(Collectors.toList());
        Page<MessagePlanVo> planVoPage = getPlanPage.toPageEntity(planVoList, queryResults.getTotal());
        log.info("leave findByConditions");
        return planVoPage;
    }
    
    @Override
    public Map<Integer, String> statusMap() {
        log.info("enter statusMap");
        log.info("leave statusMap");
        return messagePlanStatusMap;
    }
    
    // 检查传入参数
    private <T extends CreateMessagePlan> void checkMsgPlanInfo(T msgPlanInfo) {
        ServiceException.hasText(msgPlanInfo.getTitle(), bundle.getString("msg-plan-title"));
        
        ServiceException.hasText(msgPlanInfo.getText(), bundle.getString("msg-plan-content"));
        // 内容超长验证
        overLengthValid(msgPlanInfo.getText(), Current.get());
        
        ServiceException.notNull(msgPlanInfo.getExecTime(), bundle.getString("msg-plan-execute-time"));
        ServiceException.isTrue(msgPlanInfo.getExecTime().after(curTime()), bundle.getString("msg-plan-execute-time-later"));
        
        if (msgPlanInfo.getMediaIdlList() != null && msgPlanInfo.getMediaIdlList().size() > 0) {
            Assert.isTrue(msgPlanInfo.getMediaIdlList().size() <= MessageTools.MAX_MSG_MEDIA_NUM, bundle.getString("msg-plan-media-list"));
            // todo 验证资源
            msgPlanInfo.setMediaIdlList(msgPlanInfo.getMediaIdlList().stream().distinct().collect(Collectors.toList()));
            msgPlanInfo.setSms(msgPlanInfo.getMediaIdlList().size() <= 0);
        } else {
            msgPlanInfo.setSms(true);
        }
        
        // 发送消息的号码数必须大于0
        ServiceException.isTrue(msgPlanInfo.getFromNumList() != null && msgPlanInfo.getFromNumList().size() > 0,
                bundle.getString("msg-plan-from"));
        msgPlanInfo.setFromNumList(msgPlanInfo.getFromNumList().stream().distinct().collect(Collectors.toList()));
        
        // 输入的号码数必须在0-1000之间
        if (msgPlanInfo.getToNumberList() != null && msgPlanInfo.getToNumberList().size() > 0) {
            ServiceException.isTrue(msgPlanInfo.getToNumberList().size() <= 1000, bundle.getString("msg-plan-to-num-over"));
            msgPlanInfo.setToNumberList(msgPlanInfo.getToNumberList().stream().distinct().collect(Collectors.toList()));
        }
        
        // 群组数可以为0
        if (msgPlanInfo.getGroupList() != null && msgPlanInfo.getGroupList().size() > 0) {
            msgPlanInfo.setGroupList(msgPlanInfo.getGroupList().stream().distinct().collect(Collectors.toList()));
        }
        
        // 输入的号码与群组列表至少其中一个不为空
        ServiceException.isTrue((msgPlanInfo.getToNumberList() != null && msgPlanInfo.getToNumberList().size() > 0) ||
                        (msgPlanInfo.getGroupList() != null && msgPlanInfo.getGroupList().size() > 0),
                bundle.getString("msg-plan-contacts"));
    }
    
    // 获取自定义的当前时间
    private Timestamp curTime() {
        return EasyTime.init().addMinutes(-5).stamp();
    }
    
    // 检查用户套餐和信用额度
    private void validCustomerBalance(Long curId, boolean isSms, List<String> toNumList, List<Long> toGroupList) {
        // 预计接收消息号码数
        int expectMsg = 0;
        if (toGroupList != null && toGroupList.size() > 0) {
            expectMsg = contactsDao.countDistinctByCustomerIdAndGroupId(curId, toGroupList);
        } else {
            expectMsg = toNumList.size();
        }
        Optional<Customer> optional = customerDao.findById(curId);
        Assert.isTrue(optional.isPresent(), "customer does not exists");
        CustomerMarketSetting marketSetting = customerMarketSettingDao.findFirstByCustomerId(curId);
        Assert.notNull(marketSetting, "The market-setting of this customer does not exists");
        Customer customer = optional.get();
        // 计算用户可支付数
        int availablePay = isSms ? (marketSetting.getSmsTotal() + customer.getAvailableCredit().divide(marketSetting.getSmsPrice()).intValue())
                : (marketSetting.getMmsTotal() + customer.getAvailableCredit().divide(marketSetting.getMmsPrice()).intValue());
        // 判定
        ServiceException.isTrue(availablePay >= expectMsg, bundle.getString("credit-not-enough"));
    }
    
    // 验证发送消息的号码
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
    
    // 生成消息发送任务
    private MessagePlan generatePlan(CreateMessagePlan create, boolean closeToExecTime) {
        MessagePlan plan = new MessagePlan();
        plan.setCustomerId(Current.get().getId());
        create.copy(plan);
        plan.setDisable(false);
        plan.setStatus(closeToExecTime ? MessagePlanStatus.QUEUING.getValue() : MessagePlanStatus.SCHEDULING.getValue());
        plan.setMsgTotal(0);
        plan.setCreditPayNum(0);
        plan.setCreditPayCost(BigDecimal.ZERO);
        return plan;
    }
    
    // 内容超长验证提示
    private void overLengthValid(String text, CustomerVo cur) {
        if (!MessageTools.containsContactsVariables(text)) {
            String preContent = MessageTools.replaceCustomerVariables(text, cur.getFirstName(), cur.getLastName());
            ServiceException.isTrue(!MessageTools.isOverLength(preContent), MessageTools.isGsm7(preContent) ?
                    bundle.getString("msg-content-over-length-gsm7") : bundle.getString("msg-content-over-length-ucs2"));
        }
    }
    
    // 是否跨套餐
    private boolean crossedPackages(MessagePlan plan) {
        CustomerMarketSetting marketSetting = customerMarketSettingDao.findFirstByCustomerId(plan.getCustomerId());
        return marketSetting.getInvalidStatus() || plan.getUpdateTime().before(marketSetting.getOrderTime());
    }
    
    /**
     * 保存消息账单
     *
     * @param customerId
     * @param isSms
     * @param amount      (+/-)
     * @param description
     */
    private void saveMsgBill(Long customerId, boolean isSms, int amount, String description) {
        Assert.isTrue(amount != 0, "amount must not be null");
        if (isSms) {
            smsBillComponent.saveSmsBill(customerId, description, amount);
        } else {
            mmsBillComponent.saveMmsBill(customerId, description, amount);
        }
    }
    
    // 是否临近执行时间
    private boolean closeToExecTime(Timestamp execTime) {
        return execTime.before(EasyTime.init().addMinutes(5).stamp());
    }
}
