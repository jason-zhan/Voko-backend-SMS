package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingentity.CustomerMarketSetting;
import com.adbest.smsmarketingentity.MobileNumber;
import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessagePlanStatus;
import com.adbest.smsmarketingentity.QMessagePlan;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.dao.CustomerMarketSettingDao;
import com.adbest.smsmarketingfront.dao.MbNumberLibDao;
import com.adbest.smsmarketingfront.dao.MessagePlanDao;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingfront.entity.middleware.MsgPlanState;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.CreditBillComponent;
import com.adbest.smsmarketingfront.service.MessageComponent;
import com.adbest.smsmarketingfront.service.MessagePlanService;
import com.adbest.smsmarketingfront.service.MmsBillComponent;
import com.adbest.smsmarketingfront.service.SmsBillComponent;
import com.adbest.smsmarketingfront.service.param.CreateMessagePlan;
import com.adbest.smsmarketingfront.service.param.GetMessagePlanPage;
import com.adbest.smsmarketingfront.service.param.UpdateMessagePlan;
import com.adbest.smsmarketingfront.util.CommonMessage;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.EasyTime;
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
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
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
    private CreditBillComponent creditBillComponent;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    @Autowired
    private ResourceBundle bundle;
    
    @Value("${twilio.planExecTimeDelay}")
    private int planExecTimeDelay;
    @Value("${marketing.smsUnitPrice}")
    private BigDecimal smsUnitPrice;
    @Value("${marketing.mmsUnitPrice}")
    private BigDecimal mmsUnitPrice;
    
    @Autowired
    private Map<Integer, String> messagePlanStatusMap;
    
    @Transactional
    @Override
    public int create(CreateMessagePlan createPlan) {
        log.info("enter create, param={}", createPlan);
        Assert.notNull(createPlan, CommonMessage.PARAM_IS_NULL);
        // 参数校验(包括各列表属性去重)
        checkMsgPlanInfo(createPlan);
        // 检查客户号码(发送消息的号码)
        createPlan.setFromNumList(checkFromNumbers(createPlan.getFromNumList()));
        // 验证用户消息余量
        validCustomerBalance(Current.get().getId(), createPlan.getMediaIdlList() == null || createPlan.getMediaIdlList().size() == 0);
        // 持久化任务(消息结算需要任务id)
        MessagePlan plan = generatePlan(createPlan);
        messagePlanDao.save(plan);
        // 初始化中间参数实例
        MsgPlanState planState = MsgPlanState.init(plan, false);
        // 消息结算
        messageComponent.createMsgPlanSettlement(createPlan, planState);
        // 修改任务结算信息
        plan.setMsgTotal(planState.msgTotal);
        plan.setCreditPayNum(planState.creditPayNum);
        plan.setCreditPayCost(planState.creditPayCost);
        // 生成信用账单
        if (planState.creditPayCost.compareTo(BigDecimal.ZERO) > 0) {  // 若有信用额度支付，必大于0
            creditBillComponent.savePlanConsume(plan.getCustomerId(), plan.getId(), planState.creditPayCost.negate(), bundle.getString("bill-create-plan"));
        }
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
        // 参数校验(包括各列表属性去重)
        checkMsgPlanInfo(create);
        // 检查客户号码(发送消息的号码)
        create.setFromNumList(checkFromNumbers(create.getFromNumList()));
        // 验证用户消息余量
        validCustomerBalance(Current.get().getId(), create.getMediaIdlList() == null || create.getMediaIdlList().size() == 0);
        // 生成任务实例
        MessagePlan plan = generatePlan(create);
        // 持久化任务(消息结算需要任务id)
        messagePlanDao.save(plan);
        // 初始化中间参数实例
        MsgPlanState planState = MsgPlanState.init(plan, true);
        // 消息结算
        messageComponent.createMsgPlanSettlement(create, planState);
        // 修改任务结算信息
        plan.setMsgTotal(planState.msgTotal);
        plan.setCreditPayNum(planState.creditPayNum);
        plan.setCreditPayCost(planState.creditPayCost);
        // 生成信用账单
        if (planState.creditPayCost.compareTo(BigDecimal.ZERO) > 0) {  // 若有信用额度支付，必大于0
            creditBillComponent.savePlanConsume(plan.getCustomerId(), plan.getId(), planState.creditPayCost.negate(), bundle.getString("bill-create-plan"));
        }
        // TODO 将任务加入schedule容器等待执行
        
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
                        .replace("$status$", MessagePlanStatus.EDITING.getTitle())
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
        return isCross;
    }
    
    @Transactional
    @Override
    public int cancel(Long id) {
        log.info("enter cancel, id=" + id);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        // 检查任务
        CustomerVo cur = Current.get();
        MessagePlan found = messagePlanDao.findByIdAndCustomerIdAndDisableIsFalse(id, cur.getId());
        ServiceException.notNull(found, bundle.getString("msg-plan-not-exists"));
        ServiceException.isTrue(MessagePlanStatus.SCHEDULING.getValue() == found.getStatus(),
                bundle.getString("msg-plan-status").replace("$action$", bundle.getString("cancel"))
                        .replace("$status$", messagePlanStatusMap.get(MessagePlanStatus.SCHEDULING.getValue()))
        );
        // 套餐周期内，返还套餐量
        if (!crossedPackages(found)) {
            if (found.getIsSms()) {
                smsBillComponent.saveSmsBill(cur.getId(), bundle.getString("bill-cancel-plan"), found.getMsgTotal());
            } else {
                mmsBillComponent.saveMmsBill(cur.getId(), bundle.getString("bill-cancel-plan"), found.getMsgTotal());
            }
        }
        // 无论是否套餐周期内，都须返还信用消费
        if (found.getCreditPayCost().compareTo(BigDecimal.ZERO) > 0) {
            creditBillComponent.savePlanConsume(cur.getId(), found.getId(), found.getCreditPayCost(), bundle.getString("bill-cancel-plan"));
        }
        // 更新任务 - 变更为编辑中
        int cancelResult = messagePlanDao.cancelMessagePlan(found.getId(), MessagePlanStatus.EDITING.getValue(), MessagePlanStatus.SCHEDULING.getValue());
        ServiceException.isTrue(cancelResult>0, bundle.getString("msg-plan-cancel-failed"));
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
                bundle.getString("msg-plan-status").replace("$action$", bundle.getString("restart"))
                        .replace("$status$", MessagePlanStatus.EDITING.getTitle())
        );
        ServiceException.isTrue(found.getExecTime().after(EasyTime.now()),
                bundle.getString("msg-plan-execute-time-later"));
        // 更新
//        found.setStatus(MessagePlanStatus.SCHEDULING.getValue());
//        messagePlanDao.save(found);
//        int msgTotal = messageRecordDao.sumMsgNumByPlanId(found.getId());
//        // 扣除消息条数
//        if (StringUtils.hasText(found.getMediaIdList())) {
//            mmsBillComponent.saveMmsBill(cur.getId(), "restart scheduled send: " + found.getTitle(), -msgTotal);
//        } else {
//            smsBillComponent.saveSmsBill(cur.getId(), "restart scheduled send: " + found.getTitle(), -msgTotal);
//        }
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
                        .replace("$status$", MessagePlanStatus.EDITING.getTitle())
        );
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
    private <T extends CreateMessagePlan> void checkMsgPlanInfo(T msgPlanInfo) {
        ServiceException.hasText(msgPlanInfo.getTitle(), bundle.getString("msg-plan-title"));
        
        ServiceException.hasText(msgPlanInfo.getText(), bundle.getString("msg-plan-content"));
        ServiceException.isTrue(!MessageTools.isOverLength(MessageTools.trimTemplateVariables(msgPlanInfo.getText())),
                MessageTools.isGsm7(msgPlanInfo.getText()) ? bundle.getString("msg-content-over-length-gsm7") : bundle.getString("msg-content-over-length-ucs2"));
        
        ServiceException.notNull(msgPlanInfo.getExecTime(), bundle.getString("msg-plan-execute-time"));
        ServiceException.isTrue(msgPlanInfo.getExecTime().after(EasyTime.init().addMinutes(-5).stamp()), bundle.getString("msg-plan-execute-time-later"));
        
        // 内容超长验证
        overLengthValid(msgPlanInfo.getText(), Current.get());
        
        if (msgPlanInfo.getMediaIdlList() != null && msgPlanInfo.getMediaIdlList().size() > 0) {
            Assert.isTrue(msgPlanInfo.getMediaIdlList().size() <= MessageTools.MAX_MSG_MEDIA_NUM, bundle.getString("msg-plan-media-list"));
            // todo 验证资源
            msgPlanInfo.setMediaIdlList(msgPlanInfo.getMediaIdlList().stream().distinct().collect(Collectors.toList()));
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
    
    // 检查用户套餐和信用额度
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
    private MessagePlan generatePlan(CreateMessagePlan create) {
        MessagePlan plan = new MessagePlan();
        plan.setCustomerId(Current.get().getId());
        create.copy(plan);
        plan.setDisable(false);
        plan.setStatus(MessagePlanStatus.SCHEDULING.getValue());
        plan.setMsgTotal(0);
        plan.setCreditPayNum(0);
        plan.setCreditPayCost(BigDecimal.ZERO);
        return plan;
    }
    
    // 内容超长验证提示
    private void overLengthValid(String text, CustomerVo cur) {
        if (!MessageTools.containsContactsVariables(text)) {
            String preContent = MessageTools.replaceCustomerVariables(text, cur.getFirstName(), cur.getLastName());
            ServiceException.isTrue(MessageTools.isOverLength(preContent), MessageTools.isGsm7(preContent) ?
                    bundle.getString("msg-content-over-length-gsm7") : bundle.getString("msg-content-over-length-ucs2"));
        }
    }
    
    // 撤销许可
    private boolean crossedPackages(MessagePlan plan) {
        CustomerMarketSetting marketSetting = customerMarketSettingDao.findFirstByCustomerId(plan.getCustomerId());
        return marketSetting.getInvalidStatus() || plan.getUpdateTime().before(marketSetting.getOrderTime());
    }
}
