package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingentity.ContactsSource;
import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingentity.CustomerMarketSetting;
import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.MessageReturnCode;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingfront.dao.ContactsDao;
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
import com.adbest.smsmarketingfront.util.CommonMessage;
import com.adbest.smsmarketingfront.util.twilio.MessageTools;
import com.adbest.smsmarketingfront.util.twilio.param.StatusCallbackParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
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
    private SmsBillComponent smsBillComponent;
    @Autowired
    private MmsBillComponent mmsBillComponent;
    @Autowired
    private FinanceBillComponent financeBillComponent;
    @Autowired
    private CreditBillComponent creditBillComponent;
    
    @Value("${marketing.smsUnitPrice}")
    private BigDecimal smsUnitPrice;
    @Value("${marketing.mmsUnitPrice}")
    private BigDecimal mmsUnitPrice;
    @Value("${message.contacts.defaultFirstName}")
    private String contactsFirstName;
    @Value("${message.contacts.defaultLastName}")
    private String contactsLastName;
    
    @Autowired
    private ResourceBundle bundle;
    @Autowired
    private RedisTemplate redisTemplate;
    
    private static final int PAYMENT_RETRY = 5;  // 支付失败重试次数
    
    @Override
    public int updateMessageStatus(StatusCallbackParam param) {
        log.info("enter updateMessageStatus, param={}", param);
        Assert.notNull(param, CommonMessage.PARAM_IS_NULL);
        MessageReturnCode returnCode = null;
        try {
            returnCode = MessageReturnCode.valueOf(param.getMessageStatus());
        } catch (IllegalArgumentException | NullPointerException e) {
            System.out.println(e);
        }
        if (MessageReturnCode.QUEUED == returnCode) {
            return messageRecordDao.updateReturnCodeBySid(param.getMessageSid(), MessageReturnCode.QUEUED.getValue());
        }
        if (MessageReturnCode.FAILED == returnCode) {
            return messageRecordDao.updateStatusBySid(param.getMessageSid(), OutboxStatus.FAILED.getValue());
        }
        if (MessageReturnCode.SENT == returnCode) {
            return messageRecordDao.updateReturnCodeBySid(param.getMessageSid(), MessageReturnCode.SENT.getValue());
        }
        if (MessageReturnCode.DELIVERED == returnCode) {
            return messageRecordDao.updateStatusBySid(param.getMessageSid(), OutboxStatus.DELIVERED.getValue());
        }
        if (MessageReturnCode.UNDELIVERED == returnCode) {
            return messageRecordDao.updateStatusBySid(param.getMessageSid(), OutboxStatus.UNDELIVERED.getValue());
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
        Assert.isTrue(planState.msgTotal > 0, "The total number of messages is incorrectly calculated");
        if (marketSetting.getInvalidStatus() || (planState.isSms ? marketSetting.getSmsTotal() : marketSetting.getMmsTotal()) == 0) {
            // 使用信用支付
            BigDecimal creditPay = purchaseWithCredit(planState.cur.getId(), planState.isSms, planState.msgTotal);
            planState.setSettledTotal(planState.msgTotal);
            planState.setCreditPayNum(planState.msgTotal);
            planState.setCreditPayCost(creditPay.abs());
        } else {
            // 首先使用套餐支付
            int restAmount = planState.msgTotal - settlePartWithMarketSetting(marketSetting, planState.isSms, planState.msgTotal);
            if (restAmount > 0) {
                // 套餐不足部分，使用信用支付
                BigDecimal creditPay = purchaseWithCredit(planState.cur.getId(), planState.isSms, restAmount);
                planState.setCreditPayNum(restAmount);
                planState.setCreditPayCost(creditPay.abs());
            }
            planState.setSettledTotal(planState.msgTotal);
        }
        log.info("leave msgPlanSettlement");
    }
    
    @Transactional
    @Override
    public void updateMsgPlanSettlement(Long planId, int amount, boolean isSms) {
        log.info("enter updateMsgPlanSettlement, planId={}, amount={}, isSms={}", planId, amount, isSms);
        // 参数检查
        Assert.notNull(planId, "planId is null");
        Assert.isTrue(amount > 0, "amount must be greater than zero");
        Optional<MessagePlan> planOptional = messagePlanDao.findById(planId);
        Assert.isTrue(planOptional.isPresent(), "message plan does not exists");
        MessagePlan plan = planOptional.get();
        Optional<Customer> customerOptional = customerDao.findById(plan.getCustomerId());
        Assert.isTrue(customerOptional.isPresent(), "customer does not exists");
        CustomerMarketSetting marketSetting = customerMarketSettingDao.findFirstByCustomerId(plan.getCustomerId());
        Assert.notNull(marketSetting, "customer's market-setting is null");
        // 套餐校验
        ServiceException.isTrue(!marketSetting.getInvalidStatus() &&
                        plan.getUpdateTime().after(marketSetting.getOrderTime()) &&
                        plan.getUpdateTime().before(marketSetting.getInvalidTime()),
                bundle.getString("msg-plan-update-past-package")
        );
        // 余量校验
        int method = simulatedPayment(marketSetting, plan, customerOptional.get().getAvailableCredit(), amount, isSms);
        /*** 支付 & 结算 ***/
        calcChangeAndPay(method, plan, marketSetting, amount, isSms);
        log.info("leave updateMsgPlanSettlement");
    }
    
    @Transactional
    @Override
    public void validBeforeExec(Long planId) {
        log.info("enter validBeforeExec, planId={}", planId);
        // 参数检查
        Assert.notNull(planId, "planId is null");
        Optional<MessagePlan> planOptional = messagePlanDao.findById(planId);
        Assert.isTrue(planOptional.isPresent(), "message plan does not exists");
        MessagePlan plan = planOptional.get();
        Optional<Customer> customerOptional = customerDao.findById(plan.getCustomerId());
        Assert.isTrue(customerOptional.isPresent(), "customer does not exists");
        Customer customer = customerOptional.get();
        CustomerMarketSetting marketSetting = customerMarketSettingDao.findFirstByCustomerId(customer.getId());
        Assert.notNull(marketSetting, "customer's market-setting does not exists");
        // 初始化中间参数实例
        MsgPlanState planState = MsgPlanState.init(plan, new CustomerVo(customer), true);
        // 计算当前消息总量
        try {
            calcMsgTotal(planState);
        } catch (OverBudgetException e) {
            log.info("Current message volume reaches or exceeds previous budget.");
        }
        if (planState.msgTotal < plan.getMsgTotal()) {
            // 返还结算
            rebateBeforeExec(plan, marketSetting, planState);
            // 更新任务
            messagePlanDao.save(plan);
        }
        log.info("leave validBeforeExec");
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
            BigDecimal creditPay = BigDecimal.valueOf(-msgDiff).multiply(planState.isSms ? smsUnitPrice : mmsUnitPrice);
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
            creditBillComponent.savePlanConsume(plan.getCustomerId(), plan.getId(), creditChange, bundle.getString("bill-verify-exec-plan"));
        }
    }
    
    // 计算消息总数
    private void calcMsgTotal(MsgPlanState planState) {
        toNumbersTraversal(planState);
        contactsGroupsTraversal(planState);
        clearUniqueKeysCache(planState.planId);
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
            String content = "";
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
        String key = new StringBuilder(RedisKey.TMP_PLAN_UNIQUE_CONTACTS.getKey()).append(planId).append(":").append(contactsId).toString();
        return !redisTemplate.opsForValue().setIfAbsent(key, contactsId, RedisKey.TMP_PLAN_UNIQUE_CONTACTS.getExpireTime(), RedisKey.TMP_PLAN_UNIQUE_CONTACTS.getTimeUnit());
    }
    
    // 手动失效验证号码唯一性redis缓存
    private void clearUniqueKeysCache(Long planId){
        Long clearNum = redisTemplate.delete(redisTemplate.keys(RedisKey.TMP_PLAN_UNIQUE_CONTACTS.getKey() + planId + ":*"));
        log.info("clearUniqueKeysCache(clearNum={})", clearNum);
    }
    
    
    
    // 内容超长验证提示
    private void overLengthValid(String content) {
        ServiceException.isTrue(!MessageTools.isOverLength(content), MessageTools.isGsm7(content) ?
                bundle.getString("msg-content-over-length-gsm7") : bundle.getString("msg-content-over-length-ucs2"));
    }
    
    /**
     * 计算各项变化并结算
     *
     * @param method
     * @param plan
     * @param marketSetting
     * @param amount
     * @param isSms
     */
    private void calcChangeAndPay(int method, MessagePlan plan, CustomerMarketSetting marketSetting, int amount, boolean isSms) {
        int smsChange = 0;  // 套餐短信量变化
        int mmsChange = 0;  // 套餐彩信量变化
        BigDecimal creditChange = null;  // 用户信用额度变化
        if (method == 1) {  // 纯套餐支付
            if (isSms && plan.getIsSms()) {  // 本次短信 上次短信
                smsChange = plan.getMsgTotal() - amount;
            }
            if (isSms && !plan.getIsSms()) {   // 本次短信 上次彩信
                smsChange = -amount;
                mmsChange = plan.getMsgTotal();
            }
            if (!isSms && plan.getIsSms()) {   // 本次彩信 上次短信
                smsChange = plan.getMsgTotal();
                mmsChange = -amount;
            }
            if (!isSms && !plan.getIsSms()) {  // 本次彩信 上次彩信
                mmsChange = plan.getMsgTotal() - amount;
            }
        } else if (method == 2) {  // 套餐量不足，信用额度补刀
            if (isSms && plan.getIsSms()) {
                smsChange = -marketSetting.getSmsTotal();
            }
            if (isSms && !plan.getIsSms()) {
                smsChange = -marketSetting.getSmsTotal();
                mmsChange = plan.getMsgTotal();
            }
            if (!isSms && plan.getIsSms()) {
                smsChange = plan.getMsgTotal();
                mmsChange = -marketSetting.getMmsTotal();
            }
            if (!isSms && !plan.getIsSms()) {
                mmsChange = -marketSetting.getMmsTotal();
            }
            if (isSms) {
                creditChange = plan.getCreditPayCost().subtract(BigDecimal.valueOf(amount + smsChange).multiply(smsUnitPrice));
            } else {
                creditChange = plan.getCreditPayCost().subtract(BigDecimal.valueOf(amount + mmsChange).multiply(mmsUnitPrice));
            }
        }
        if (smsChange != 0) {
            smsBillComponent.saveSmsBill(plan.getCustomerId(), bundle.getString("bill-update-plan"), smsChange);
        }
        if (mmsChange != 0) {
            mmsBillComponent.saveMmsBill(plan.getCustomerId(), bundle.getString("bill-update-plan"), mmsChange);
        }
        if (creditChange != null && creditChange.compareTo(BigDecimal.ZERO) != 0) {
            creditBillComponent.savePlanConsume(plan.getCustomerId(), plan.getId(), creditChange, bundle.getString("bill-update-plan"));
        }
    }
    
    @Transactional
    @Override
    public void autoReplySettlement(Long customerId, int amount, boolean isSms) {
        log.info("enter autoReplySettlement, customerId={}, amount={}, isSms={}", customerId, amount, isSms);
        // 参数检查
        Assert.isTrue(amount > 0, "amount must be greater than zero.");
        CustomerMarketSetting marketSetting = customerMarketSettingDao.findFirstByCustomerId(customerId);
        Assert.notNull(marketSetting, "customer's market-setting is null");
        if (marketSetting.getInvalidStatus() || (isSms ? marketSetting.getSmsTotal() : marketSetting.getMmsTotal()) == 0) {
            // 使用信用结算
            BigDecimal creditPay = purchaseWithCredit(customerId, isSms, amount);
            // 产生金融账单
            financeBillComponent.saveFinanceBill(customerId, creditPay, bundle.getString("bill-reply-msg"));
        } else {
            // 首先使用套餐余量结算
            int restAmount = amount - settlePartWithMarketSetting(marketSetting, isSms, amount);
            if (restAmount > 0) {
                // 套餐余量不足部分，使用信用结算
                BigDecimal creditPay = purchaseWithCredit(customerId, isSms, restAmount);
                // 产生金融账单
                financeBillComponent.saveFinanceBill(customerId, creditPay, bundle.getString("bill-reply-msg"));
            }
        }
        // 产生消息账单
        saveMsgBill(customerId, isSms, -amount, bundle.getString("bill-reply-msg"));
        log.info("leave autoReplySettlement");
    }
    
    
//    /**
//     * 使用套餐余量结算
//     *
//     * @param customerId
//     * @param isSms
//     * @param amount
//     * @return 是否结算成功
//     */
//    private boolean settleWithMarketSetting(Long customerId, boolean isSms, int amount) {
//        // 使用套餐余量结算
//        int msResult;
//        if (isSms) {
//            msResult = customerMarketSettingDao.updateSmsByCustomerId(customerId, -amount);
//        } else {
//            msResult = customerMarketSettingDao.updateMmsByCustomerId(customerId, -amount);
//        }
//        return msResult > 0;
//    }
    
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
    private BigDecimal purchaseWithCredit(Long customerId, boolean isSms, int amount) {
        BigDecimal payCredit = BigDecimal.valueOf(amount).multiply(isSms ? smsUnitPrice : mmsUnitPrice);
        int result = customerDao.updateCredit(customerId, payCredit.negate());
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
     * 模拟支付 以预知支付结果
     *
     * @param marketSetting
     * @param plan
     * @param realCredit    用户当前信用额度
     * @param msgAmount
     * @param isSms
     */
    private int simulatedPayment(CustomerMarketSetting marketSetting, MessagePlan plan, BigDecimal realCredit, int msgAmount, boolean isSms) {
        // 套餐余量
        int packageTotal = isSms ?
                (plan.getIsSms() ? marketSetting.getSmsTotal() + plan.getMsgTotal() : marketSetting.getSmsTotal())
                :
                (plan.getIsSms() ? marketSetting.getMmsTotal() : marketSetting.getMmsTotal() + plan.getMsgTotal());
        BigDecimal creditTotal = realCredit.add(plan.getCreditPayCost());
        // 首先使用套餐支付
        int restAmount = msgAmount - packageTotal;
        if (restAmount <= 0) {  // 套餐余量足以支付
            return 1;
        }
        // 不足部分使用信用额度支付
        BigDecimal nowCreditPay = BigDecimal.valueOf(restAmount).multiply(isSms ? smsUnitPrice : mmsUnitPrice);
        ServiceException.isTrue(creditTotal.compareTo(nowCreditPay) >= 0, bundle.getString("credit-not-enough"));
        return 2;
    }
    
}
