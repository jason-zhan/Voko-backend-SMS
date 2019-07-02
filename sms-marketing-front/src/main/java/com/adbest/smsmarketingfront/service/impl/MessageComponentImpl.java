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
import com.adbest.smsmarketingfront.util.EasyTime;
import com.adbest.smsmarketingfront.util.twilio.param.StatusCallbackParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    
    @Transactional
    @Override
    public void createMsgPlanSettlement(Long customerId, Long planId, int amount, boolean isSms) {
        log.info("enter createMsgPlanSettlement, customerId={}, planId={}, amount={}, isSms={}", customerId, planId, amount, isSms);
        // 参数检查
        Assert.notNull(planId, "planId is null");
        Assert.isTrue(amount > 0, "amount must be greater than zero");
        CustomerMarketSetting marketSetting = customerMarketSettingDao.findFirstByCustomerId(customerId);
        Assert.notNull(marketSetting, "customer's market-setting is null");
        if (marketSetting.getInvalidStatus() || (isSms ? marketSetting.getSmsTotal() : marketSetting.getMmsTotal()) == 0) {
            // 使用信用支付
            BigDecimal creditPay = purchaseWithCredit(customerId, isSms, amount);
            // 生成信用账单
            creditBillComponent.savePlanConsume(customerId, planId, creditPay, bundle.getString("bill-create-plan"));
        } else {
            // 首先使用套餐支付
            int restAmount = amount - settlePartWithMarketSetting(marketSetting, isSms, amount);
            if (restAmount > 0) {
                // 套餐不足部分，使用信用支付
                BigDecimal creditPay = purchaseWithCredit(customerId, isSms, restAmount);
                // 生成信用账单
                creditBillComponent.savePlanConsume(customerId, planId, creditPay.negate(), bundle.getString("bill-create-plan"));
            }
        }
        // 产生消息账单
        saveMsgBill(customerId, isSms, -amount, bundle.getString("bill-create-plan"));
        log.info("leave createMsgPlanSettlement");
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
    
    @Override
    public void validBeforeExec(Long planId) {
        // TODO
    }
    
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
    
    
    /**
     * 使用套餐余量结算
     *
     * @param customerId
     * @param isSms
     * @param amount
     * @return 是否结算成功
     */
    private boolean settleWithMarketSetting(Long customerId, boolean isSms, int amount) {
        // 使用套餐余量结算
        int msResult;
        if (isSms) {
            msResult = customerMarketSettingDao.updateSmsByCustomerId(customerId, -amount);
        } else {
            msResult = customerMarketSettingDao.updateMmsByCustomerId(customerId, -amount);
        }
        return msResult > 0;
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
            msResult = customerMarketSettingDao.updateSmsByCustomerId(marketSetting.getCustomerId(), -availableAmount);
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
