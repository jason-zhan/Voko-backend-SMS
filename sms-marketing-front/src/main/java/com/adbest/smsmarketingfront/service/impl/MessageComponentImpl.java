package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingentity.CustomerMarketSetting;
import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessageReturnCode;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.dao.CustomerMarketSettingDao;
import com.adbest.smsmarketingfront.dao.MessagePlanDao;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.CreditBillComponent;
import com.adbest.smsmarketingfront.service.FinanceBillComponent;
import com.adbest.smsmarketingfront.service.MessageComponent;
import com.adbest.smsmarketingfront.service.MmsBillComponent;
import com.adbest.smsmarketingfront.service.SmsBillComponent;
import com.adbest.smsmarketingfront.util.CommonMessage;
import com.adbest.smsmarketingfront.util.twilio.param.StatusCallbackParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
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
    
    @Autowired
    private ResourceBundle bundle;
    
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
    
    @Override
    public void messagePlanSettlement(Long customerId, Long planId, int amount, boolean isSms) {
        log.info("enter messagePlanSettlement, customerId={}, planId={}, amount={}, isSms={}", customerId, planId, amount, isSms);
        // 参数检查
        Assert.notNull(planId, "planId is null");
        Assert.isTrue(amount > 0, "amount must be greater than zero");
        Optional<MessagePlan> optional = messagePlanDao.findById(planId);
        int execMsAmount;  // 套餐消息数量变化
        BigDecimal execCredit;  // 信用额度变化
        if (optional.isPresent()) {  // 修改
            MessagePlan plan = optional.get();
            // 1.小于上次套餐支付条数
            if (amount <= (plan.getMsgTotal() - plan.getPayNum())) {
            
            } else if (amount > (plan.getMsgTotal() - plan.getPayNum()) && amount <= plan.getMsgTotal()) {
            
            }
            // 2.大于上次套餐支付数 且 小于上次总支付数
            // 1.大于上次总支付数
            
        } else {  // 新增
            execMsAmount = -amount;
            execCredit = BigDecimal.valueOf(-amount).multiply(isSms ? smsUnitPrice : mmsUnitPrice);
        }
        
        
        log.info("leave messagePlanSettlement");
    }
    
    @Transactional
    @Override
    public void autoReplySettlement(Long customerId, int amount, boolean isSms) {
        log.info("enter autoReplySettlement, customerId={}, amount={}, isSms={}", customerId, amount, isSms);
        // 参数检查
        Assert.isTrue(amount > 0, "amount must be greater than zero.");
        // 使用套餐结算
        int restAmount = amount - settleWithMarketSetting(customerId, isSms, amount);
        if (restAmount > 0) {
            // 使用信用结算
            BigDecimal cost = purchaseWithCredit(customerId, isSms, amount);
            // 产生金融账单
            financeBillComponent.saveFinanceBill(customerId, cost, bundle.getString("auto-reply-msg"));
        }
        // 产生消息账单
        saveMsgBill(customerId, isSms, amount, bundle.getString("auto-reply-msg"));
        log.info("leave autoReplySettlement");
    }
    
    
    /**
     * 使用套餐余量结算
     *
     * @param customerId
     * @param isSms
     * @param amount
     * @return 套餐结算的数量
     */
    private int settleWithMarketSetting(Long customerId, boolean isSms, int amount) {
        // 使用套餐余量结算
        int msResult;
        if (isSms) {
            msResult = customerMarketSettingDao.updateSmsByCustomerId(customerId, -amount);
        } else {
            msResult = customerMarketSettingDao.updateMmsByCustomerId(customerId, -amount);
        }
        if (msResult > 0) {  // 套餐结算成功
            return amount;
        }
        // 结算失败
        CustomerMarketSetting marketSetting = customerMarketSettingDao.findFirstByCustomerId(customerId);
        Assert.notNull(marketSetting, "customer's marketSetting is not exists");
        // 使用套餐余量部分结算
        int availableAmount = 0;
        if (isSms && marketSetting.getSmsTotal() > 0) {
            availableAmount = marketSetting.getSmsTotal() > amount ? amount : marketSetting.getSmsTotal();
            msResult = customerMarketSettingDao.updateSmsByCustomerId(customerId, -availableAmount);
        }
        if (!isSms && marketSetting.getMmsTotal() > 0) {
            availableAmount = marketSetting.getMmsTotal() > amount ? amount : marketSetting.getMmsTotal();
            msResult = customerMarketSettingDao.updateSmsByCustomerId(customerId, -availableAmount);
        }
        if (msResult > 0) {  // 套餐部分结算成功
            return availableAmount;
        }
        return 0;
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
     * @param amount
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
    
}
