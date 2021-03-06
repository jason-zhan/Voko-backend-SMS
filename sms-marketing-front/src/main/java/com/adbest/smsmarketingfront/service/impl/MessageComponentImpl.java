package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingentity.ContactsSource;
import com.adbest.smsmarketingentity.CreditBillType;
import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingentity.CustomerMarketSetting;
import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessagePlanStatus;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.MessageReturnCode;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingfront.dao.ContactsDao;
import com.adbest.smsmarketingfront.dao.CreditBillDao;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.dao.CustomerMarketSettingDao;
import com.adbest.smsmarketingfront.dao.MessagePlanDao;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingfront.entity.enums.RedisKey;
import com.adbest.smsmarketingfront.entity.middleware.MsgPlanState;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.handler.plan.OverBudgetException;
import com.adbest.smsmarketingfront.service.CreditBillComponent;
import com.adbest.smsmarketingfront.service.FinanceBillComponent;
import com.adbest.smsmarketingfront.service.MessageComponent;
import com.adbest.smsmarketingfront.service.MmsBillComponent;
import com.adbest.smsmarketingfront.service.SmsBillComponent;
import com.adbest.smsmarketingfront.util.twilio.MessageTools;
import com.twilio.rest.api.v2010.account.Message.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
@Slf4j
public class MessageComponentImpl implements MessageComponent {
    
    @Autowired
    MessagePlanDao messagePlanDao;
    @Autowired
    private MessageRecordDao messageRecordDao;
    @Autowired
    private CustomerDao customerDao;
    @Autowired
    private ContactsDao contactsDao;
    @Autowired
    private CustomerMarketSettingDao customerMarketSettingDao;
    @Autowired
    private CreditBillDao creditBillDao;
    
    @Autowired
    private SmsBillComponent smsBillComponent;
    @Autowired
    private MmsBillComponent mmsBillComponent;
    @Autowired
    private FinanceBillComponent financeBillComponent;
    @Autowired
    private CreditBillComponent creditBillComponent;
    
    @Value("${message.contacts.defaultFirstName}")
    private String contactsFirstName;
    @Value("${message.contacts.defaultLastName}")
    private String contactsLastName;
    
    @Autowired
    private ResourceBundle bundle;
    @Autowired
    private RedisTemplate redisTemplate;
    
    @Override
    public int updateMessageStatus(String sid, String status) {
        log.info("enter updateMessageStatus, sid={}, status={}", sid, status);
        Assert.hasText(sid, "sid is empty");
        Assert.hasText(status, "status is empty");
        if (Status.QUEUED.toString().equals(status)) {
            return messageRecordDao.updateReturnCodeBySid(sid, MessageReturnCode.QUEUED.getValue());
        }
        if (Status.FAILED.toString().equals(status)) {
            return messageRecordDao.updateReturnCodeAndStatusBySid(sid, MessageReturnCode.FAILED.getValue(), OutboxStatus.FAILED.getValue());
        }
        if (Status.SENT.toString().equals(status)) {
            return messageRecordDao.updateReturnCodeBySid(sid, MessageReturnCode.SENT.getValue());
        }
        if (Status.DELIVERED.toString().equals(status)) {
            return messageRecordDao.updateMsgForDelivered(sid, MessageReturnCode.DELIVERED.getValue(), OutboxStatus.DELIVERED.getValue());
        }
        if (Status.UNDELIVERED.toString().equals(status)) {
            return messageRecordDao.updateReturnCodeAndStatusBySid(sid, MessageReturnCode.UNDELIVERED.getValue(), OutboxStatus.UNDELIVERED.getValue());
        }
        log.info("leave updateMessageStatus");
        return 0;
    }
    
    @Transactional
    @Override
    public void msgPlanSettlement(MsgPlanState planState) {
        log.info("enter msgPlanSettlement, planState={}", planState);
        CustomerMarketSetting marketSetting = customerMarketSettingDao.findFirstByCustomerId(planState.cur.getId());
        Assert.notNull(marketSetting, "customer's market-setting is null");
        // 计算消息总量
        calcMsgTotal(planState);
        ServiceException.isTrue(planState.msgTotal <= 100000, bundle.getString("msg-total-max"));
        Assert.isTrue(planState.msgTotal > 0, "The total number of messages is incorrectly calculated");
        if (marketSetting.getInvalidStatus() || (planState.isSms ? marketSetting.getSmsTotal() : marketSetting.getMmsTotal()) == 0) {
            // 使用信用支付
            BigDecimal creditPay = purchaseWithCredit(planState.cur.getId(), planState.isSms, planState.msgTotal, marketSetting);
            planState.setSettledTotal(planState.msgTotal);
            planState.setCreditPayNum(planState.msgTotal);
            planState.setCreditPayCost(creditPay.abs());
        } else {
            // 首先使用套餐支付
            int restAmount = planState.msgTotal - settlePartWithMarketSetting(marketSetting, planState.isSms, planState.msgTotal);
            if (restAmount > 0) {
                // 套餐不足部分，使用信用支付
                BigDecimal creditPay = purchaseWithCredit(planState.cur.getId(), planState.isSms, restAmount, marketSetting);
                planState.setCreditPayNum(restAmount);
                planState.setCreditPayCost(creditPay.abs());
            }
            planState.setSettledTotal(planState.msgTotal);
        }
        log.info("leave msgPlanSettlement");
    }
    
    @Transactional
    @Override
    public MessagePlan validBeforeExec(Long planId) {
        log.info("enter validBeforeExec, planId={}", planId);
        // 参数检查
        Assert.notNull(planId, "planId is null");
        Optional<MessagePlan> planOptional = messagePlanDao.findById(planId);
        Assert.isTrue(planOptional.isPresent(), "message plan does not exists");
        MessagePlan found = planOptional.get();
        Optional<Customer> customerOptional = customerDao.findById(found.getCustomerId());
        Assert.isTrue(customerOptional.isPresent(), "customer does not exists");
        Customer customer = customerOptional.get();
        CustomerMarketSetting marketSetting = customerMarketSettingDao.findFirstByCustomerId(customer.getId());
        Assert.notNull(marketSetting, "customer's market-setting does not exists");
        // 初始化中间参数实例
        MsgPlanState planState = MsgPlanState.init(found, new CustomerVo(customer), true);
        // 计算当前消息总量
        try {
            calcMsgTotal(planState);
        } catch (OverBudgetException e) {
            log.info("Current message volume reaches or exceeds previous budget.");
        }
        if (planState.msgTotal < found.getMsgTotal()) {
            // 返还结算
            rebateBeforeExec(found, marketSetting, planState);
            // 更新任务
            messagePlanDao.save(found);
        }
        log.info("leave validBeforeExec");
        return found;
    }
    
    private void rebateBeforeExec(MessagePlan plan, CustomerMarketSetting marketSetting, MsgPlanState planState) {
        int creditLine = plan.getMsgTotal() - plan.getCreditPayNum(); // 信用支付基线
        int msgDiff = creditLine - planState.msgTotal;  // 信用支付基线 与 总消息数 之差
        int smsChange = 0;
        int mmsChange = 0;
        BigDecimal creditChange;
        // 判断当前是否跨套餐
        boolean outerPackage = marketSetting.getInvalidStatus() || plan.getUpdateTime().before(marketSetting.getOrderTime());
        if (msgDiff < 0) {
            BigDecimal creditPay = BigDecimal.valueOf(-msgDiff).multiply(planState.isSms ? marketSetting.getSmsPrice() : marketSetting.getMmsPrice());
            creditChange = plan.getCreditPayCost().subtract(creditPay);
            plan.setCreditPayNum(-msgDiff);
            plan.setCreditPayCost(creditPay);
        } else {
            creditChange = plan.getCreditPayCost().abs();
            plan.setCreditPayNum(0);
            plan.setCreditPayCost(BigDecimal.ZERO);
            smsChange = planState.isSms ? msgDiff : 0;
            mmsChange = planState.isSms ? 0 : msgDiff;
        }
        plan.setMsgTotal(planState.msgTotal);
        // 结算
        if (smsChange > 0 && !outerPackage) {
            smsBillComponent.saveSmsBill(plan.getCustomerId(), bundle.getString("bill-verify-exec-plan"), smsChange);
        }
        if (mmsChange > 0 && !outerPackage) {
            mmsBillComponent.saveMmsBill(plan.getCustomerId(), bundle.getString("bill-verify-exec-plan"), mmsChange);
        }
        if (creditChange != null && creditChange.compareTo(BigDecimal.ZERO) > 0) {
            customerDao.paymentByCredit(plan.getCustomerId(), creditChange);
            creditBillComponent.savePlanConsume(plan.getCustomerId(), plan.getId(), creditChange, bundle.getString("bill-verify-exec-plan"));
        }
    }
    
    // 计算消息总数
    private void calcMsgTotal(MsgPlanState planState) {
        clearUniqueKeysCache(planState.planId);
        toNumbersTraversal(planState);
        contactsGroupsTraversal(planState);
    }
    
    // 遍历输入的手机号并计算消息量
    private void toNumbersTraversal(MsgPlanState planState) {
        if (planState.toNumList == null || planState.toNumList.size() == 0) {
            return;
        }
        List<MessageRecord> messageList = new ArrayList<>();
        List<Contacts> newContactsList = new ArrayList<>();
        for (String number : planState.toNumList) {
            // 存在性验证
            Contacts contacts = contactsDao.findFirstByCustomerIdAndPhone(planState.cur.getId(), number);
            if (contacts == null) {
                // 不存在，生成联系人并加入新建联系人列表
                contacts = generateContacts(planState.cur.getId(), number);
                newContactsList.add(contacts);
                continue;
            }
            calcMsg(messageList, planState, contacts);
        }
        // 批量持久化增加的联系人并计算消息数
        if (newContactsList.size() > 0) {
            contactsDao.saveAll(newContactsList);
            String content = planState.preContent;
            if (planState.contactsVars) {
                // 默认联系人名称和姓氏
                // TODO 内容优化
                content = MessageTools.replaceContactsVariables(planState.preContent, contactsFirstName, contactsLastName);
                overLengthValid(content);
            }
            // 计算分段数 (新增的联系人使用默认名称和姓氏，使得所有消息长度相同)
            int segments = planState.isSms ? (planState.contactsVars ? MessageTools.calcSmsSegments(content) : planState.preSegments) : 1;
            planState.msgTotal += segments * newContactsList.size();
            if (planState.saveMsg) {
                for (Contacts contacts : newContactsList) {
                    // 新增的号码，跳过各项验证
                    counterCore(planState, messageList, content, segments, contacts);
                }
            }
        }
        // 批量保存消息
        if (messageList.size() > 0) {
            messageRecordDao.saveAll(messageList);
        }
    }
    
    // 计数核心
    private void counterCore(MsgPlanState planState, List<MessageRecord> messageList, String content, int segments, Contacts contacts) {
        messageList.add(generateMessage(
                planState,
                contacts,
                planState.fromNumList.get(planState.counter % planState.fromNumList.size()),
                content,
                segments
        ));
        planState.counter++;
        if (planState.settledTotal > 0 && planState.msgTotal >= planState.settledTotal) {
            messageRecordDao.saveAll(messageList);
            throw new OverBudgetException();
        }
    }
    
    // 遍历所有群组并计算消息量
    private void contactsGroupsTraversal(MsgPlanState planState) {
        if (planState.toGroupList == null || planState.toGroupList.size() == 0) {
            return;
        }
        for (Long groupId : planState.toGroupList) {
            groupTraversal(groupId, planState);
        }
    }
    
    // 遍历群组所有联系人并计算生成的消息数
    private void groupTraversal(Long groupId, MsgPlanState planState) {
        Page<Contacts> contactsPage = null;
        Pageable pageable = PageRequest.of(0, 1000);
        do {
            contactsPage = contactsDao.findUsableByCustomerIdAndGroupId(planState.cur.getId(), groupId, pageable);
            // 群组验证
            if (contactsPage.isEmpty()) {
                break;
            }
            List<MessageRecord> messageList = new ArrayList<>();
            for (Contacts contacts : contactsPage.getContent()) {
                calcMsg(messageList, planState, contacts);
            }
            // 批量保存消息
            if (messageList.size() > 0) {
                messageRecordDao.saveAll(messageList);
            }
            pageable = pageable.next();
        } while (contactsPage.hasNext());
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
    
    private void calcMsg(List<MessageRecord> messageList, MsgPlanState planState, Contacts contacts) {
        //  合法性验证
        if (contacts.getIsDelete() || contacts.getInLock()) {
            return;
        }
        //  唯一性验证
        if (isRepeatRecipient(planState.planId, contacts.getId())) {
            return;
        }
        //  计算内容
        String content = planState.preContent;
        if (planState.contactsVars) {
            content = MessageTools.replaceContactsVariables(planState.preContent, contacts.getFirstName(), contacts.getLastName());
            overLengthValid(content);
        }
        //  计算分段数
        int segments = planState.isSms ? (planState.contactsVars ? MessageTools.calcSmsSegments(content) : planState.preSegments) : 1;
        planState.msgTotal += segments;
        //  持久化消息
        if (planState.saveMsg) {
            counterCore(planState, messageList, content, segments, contacts);
        }
    }
    
    /**
     * 验证接收消息号码是否重复
     *
     * @param planId
     * @param contactsId
     * @return true:repeat | false:absent
     */
    private boolean isRepeatRecipient(Long planId, Long contactsId) {
        String key = new StringBuilder(RedisKey.MSG_PLAN_UNIQUE_CONTACTS.getKey()).append(planId).append(":").append(contactsId).toString();
        return !redisTemplate.opsForValue().setIfAbsent(key, contactsId, RedisKey.MSG_PLAN_UNIQUE_CONTACTS.getExpireTime(), RedisKey.MSG_PLAN_UNIQUE_CONTACTS.getTimeUnit());
    }
    
    // 手动失效验证号码唯一性redis缓存
    private void clearUniqueKeysCache(Long planId) {
        Long clearNum = redisTemplate.delete(redisTemplate.keys(RedisKey.MSG_PLAN_UNIQUE_CONTACTS.getKey() + planId + ":*"));
        log.info("clearUniqueKeysCache(clearNum={})", clearNum);
    }
    
    
    // 内容超长验证提示
    private void overLengthValid(String content) {
        ServiceException.isTrue(!MessageTools.isOverLength(content), MessageTools.isGsm7(content) ?
                bundle.getString("msg-content-over-length-gsm7") : bundle.getString("msg-content-over-length-ucs2"));
    }
    
    
    @Transactional
    @Override
    public void autoReplySettlement(MessageRecord message, String remark) {
        log.info("enter autoReplySettlement, message={}, remark={}", message, remark);
        // 参数检查
        Assert.isTrue(message.getSegments() > 0, "amount of messages must be greater than zero.");
        Assert.hasText(remark, "remark is empty.");
        // 首先尝试套餐余量支付
        int packageResult = 0;
        if (message.getSms()) {
            packageResult = customerMarketSettingDao.updateSmsByCustomerId(message.getCustomerId(), message.getSegments());
        } else {
            packageResult = customerMarketSettingDao.updateMmsByCustomerId(message.getCustomerId(), message.getSegments());
        }
        // 若套餐余量支付失败，则信用支付
        if (packageResult > 0) {
            message.setCost(BigDecimal.ZERO);
        } else {
            CustomerMarketSetting marketSetting = customerMarketSettingDao.findFirstByCustomerId(message.getCustomerId());
            Assert.notNull(marketSetting, "customer's market-setting is null");
            BigDecimal creditPay = purchaseWithCredit(message.getCustomerId(), message.getSms(), message.getSegments(), marketSetting);
            financeBillComponent.saveFinanceBill(message.getCustomerId(), creditPay.negate(), remark);
            message.setCost(creditPay);
        }
        // 产生消息账单
        saveMsgBill(message.getCustomerId(), message.getSms(), -message.getSegments(), remark);
        log.info("leave autoReplySettlement");
    }
    
    @Override
    @Transactional
    public void autoReplyReturn(MessageRecord message, String remark) {
        log.info("enter autoReplyReturn, message={}, remark={}", message, remark);
        Assert.isTrue(message.getSegments() > 0, "amount of messages must be greater than zero.");
        Assert.hasText(remark, "remark is empty.");
        if (message.getCost() == null || message.getCost().compareTo(BigDecimal.ZERO) == 0) {
            // 套餐支付，若未跨套餐，则更新用户套餐余量
            CustomerMarketSetting marketSetting = customerMarketSettingDao.findFirstByCustomerId(message.getCustomerId());
            Assert.notNull(marketSetting, "customer's market-setting is null");
            if (marketSetting.getInvalidStatus() || marketSetting.getOrderTime().after(message.getSendTime())) {
                log.info("leave autoReplyReturn [market-setting invalid]");
                return;
            }
            if (message.getSms()) {
                customerMarketSettingDao.updateSmsByCustomerId(message.getCustomerId(), message.getSegments());
            } else {
                customerMarketSettingDao.updateMmsByCustomerId(message.getCustomerId(), message.getSegments());
            }
        } else {
            Assert.isTrue(message.getCost().compareTo(BigDecimal.ZERO) > 0, "the cost of message must be greater than zero.");
            // 信用支付，更新用户信用额度并保存金融账单
            customerDao.paymentByCredit(message.getCustomerId(), message.getCost());
            financeBillComponent.saveFinanceBill(message.getCustomerId(), message.getCost(), remark);
        }
        saveMsgBill(message.getCustomerId(), message.getSms(), message.getSegments(), remark);
        log.info("leave autoReplyReturn");
    }
    
    @Transactional
    @Override
    @Async
    public void validAndFinishPlan(MessagePlan plan) {
        log.info("enter validAndFinishPlan, plan={}", plan);
        if (messageRecordDao.existsByPlanIdAndStatus(plan.getId(), OutboxStatus.SENT.getValue())) {
            return;
        }
        // 统计发送失败的数量
        int failedMsg = messageRecordDao.sumMsgByPlanIdAndStatus(plan.getId(), OutboxStatus.FAILED.getValue());
        if (failedMsg == 0) {
            // 无失败消息，直接完成任务
            planFinalSettlement(plan);
            log.info("finish message plan: {}", plan.getId());
            return;
        }
        // 发送失败数大于0，保存消息账单
        saveMsgBill(plan.getCustomerId(), plan.getIsSms(), failedMsg, bundle.getString("bill-return-failed"));
        // 计算信用额度和套餐余量返还量
        BigDecimal creditChange = BigDecimal.ZERO;
        int msgChange = 0;
        if (plan.getCreditPayNum() > 0) {
            int numDiff = plan.getCreditPayNum() - failedMsg;
            if (numDiff > 0) {
                creditChange = plan.getCreditPayCost().multiply(BigDecimal.valueOf(failedMsg)).divide(BigDecimal.valueOf(plan.getCreditPayNum()));
            } else {
                creditChange = plan.getCreditPayCost();
                msgChange = -numDiff;
            }
        } else {
            msgChange = failedMsg;
        }
        // 返还信用额度和套餐余量
        if (creditChange.compareTo(BigDecimal.ZERO) > 0) {
            customerDao.paymentByCredit(plan.getCustomerId(), creditChange);
            creditBillComponent.savePlanConsume(plan.getCustomerId(), plan.getId(), creditChange, bundle.getString("bill-return-failed"));
        }
        if (msgChange > 0) {
            if (plan.getIsSms()) {
                customerMarketSettingDao.updateSmsByCustomerId(plan.getCustomerId(), msgChange);
            } else {
                customerMarketSettingDao.updateMmsByCustomerId(plan.getCustomerId(), msgChange);
            }
        }
        // 总结算/完成任务
        planFinalSettlement(plan);
        log.info("finish message plan: {}", plan.getId());
        log.info("leave validAndFinishPlan");
    }
    
    /**
     * 使用套餐余量部分结算
     *
     * @param marketSetting
     * @param isSms
     * @param amount
     * @return 套餐结算的量
     */
    private int settlePartWithMarketSetting(CustomerMarketSetting marketSetting, boolean isSms, int amount) {
        // 使用套餐余量部分结算
        int availableAmount = 0;
        int msResult = 0;
        if (isSms && marketSetting.getSmsTotal() > 0) {
            availableAmount = marketSetting.getSmsTotal() > amount ? amount : marketSetting.getSmsTotal();
            msResult = customerMarketSettingDao.updateSmsByCustomerId(marketSetting.getCustomerId(), -availableAmount);
        }
        if (!isSms && marketSetting.getMmsTotal() > 0) {
            availableAmount = marketSetting.getMmsTotal() > amount ? amount : marketSetting.getMmsTotal();
            msResult = customerMarketSettingDao.updateMmsByCustomerId(marketSetting.getCustomerId(), -availableAmount);
        }
        if (msResult > 0) {  // 套餐部分结算成功
            return availableAmount;
        }
        return msResult;
    }
    
    /**
     * 使用信用额度购买消息量
     *
     * @param customerId
     * @param isSms
     * @param amount
     * @return 支付金额
     */
    private BigDecimal purchaseWithCredit(Long customerId, boolean isSms, int amount, CustomerMarketSetting marketSetting) {
        BigDecimal payCredit = BigDecimal.valueOf(amount).multiply(isSms ? marketSetting.getSmsPrice() : marketSetting.getMmsPrice());
        int result = customerDao.paymentByCredit(customerId, payCredit.negate());
        ServiceException.isTrue(result > 0, bundle.getString("credit-not-enough"));
        return payCredit;
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
    
    /**
     * 消息发送任务最终结算
     *
     * @param plan
     */
    private void planFinalSettlement(MessagePlan plan) {
        // 统计该任务信用消费
        BigDecimal totalAmount = creditBillDao.sumAmountByTypeAndReferId(CreditBillType.MESSAGE_PLAN.getValue(), plan.getId());
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            // 产生金融账单
            financeBillComponent.saveFinanceBill(plan.getCustomerId(), totalAmount, bundle.getString("bill-final-settle-plan"));
        }
        // 更新任务状态 - 已完成
        messagePlanDao.updateStatusById(plan.getId(), MessagePlanStatus.FINISHED.getValue(), MessagePlanStatus.EXECUTION_COMPLETED.getValue());
    }
    
}
