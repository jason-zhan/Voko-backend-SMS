package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingentity.CustomerMarketSetting;
import com.adbest.smsmarketingentity.MessageReturnCode;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.dao.CustomerMarketSettingDao;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingfront.service.MessageComponent;
import com.adbest.smsmarketingfront.service.MmsBillComponent;
import com.adbest.smsmarketingfront.service.SmsBillComponent;
import com.adbest.smsmarketingfront.util.CommonMessage;
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
    private MessageRecordDao messageRecordDao;
    @Autowired
    private CustomerDao customerDao;
    @Autowired
    private CustomerMarketSettingDao customerMarketSettingDao;
    
    @Autowired
    private SmsBillComponent smsBillComponent;
    @Autowired
    private MmsBillComponent mmsBillComponent;
    
    @Value("${marketing.singleSmsPrice}")
    private BigDecimal singleSmsPrice;
    @Value("${marketing.singleMmsPrice}")
    private BigDecimal singleMmsPrice;
    @Value("${marketing.paymentCredit}")
    private BigDecimal paymentCredit;
    
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
    public void messageSettlement(Long customerId, boolean isSms, int amount, String describe) {
        log.info("enter messageSettlement, customerId={}, isSms={}, amount={}", customerId, isSms, amount);
        // 参数校验
        Assert.isTrue(amount > 0, "amount must be greater than zero.");
        Assert.hasText(describe, "describe is empty");
        // 使用套餐余量结算
        int updateMsResult = settleWithMarketSetting(customerId, isSms, amount);
        // 套餐余量不足，使用信用额度结算
        if (updateMsResult < 0) {
            // 套餐余量完成部分结算
            int payedNum = settlePartWithMarketSetting(customerId, isSms, amount);
            // 剩余部分通过信用额度购买
            purchaseWithCredit(customerId, isSms, amount - payedNum);
        }
        // 保存消息账单
        if (isSms) {
            smsBillComponent.saveSmsBill(customerId, describe, amount);
        } else {
            mmsBillComponent.saveMmsBill(customerId, describe, amount);
        }
        log.info("leave messageSettlement");
    }
    
    /**
     * 使用套餐余量结算
     *
     * @param customerId
     * @param isSms
     * @param amount
     * @return
     */
    private int settleWithMarketSetting(Long customerId, boolean isSms, int amount) {
        int retried = 0;
        while (retried < PAYMENT_RETRY) {
            int result;
            // 更新套餐内短信或彩信余量
            if (isSms) {
                result = customerMarketSettingDao.updateSmsByCustomerId(customerId, -amount);
            } else {
                result = customerMarketSettingDao.updateMmsByCustomerId(customerId, -amount);
            }
            if (result > 0) {  // 更新成功，跳出循环
                return result;
            }
            // 更新失败
            CustomerMarketSetting marketSetting = customerMarketSettingDao.findFirstByCustomerId(customerId);
            Assert.notNull(marketSetting, "customer's marketSetting is not exists");
            if ((isSms ? marketSetting.getSmsTotal() : marketSetting.getMmsTotal()) < amount) { // 套餐余量不足，跳出循环
                return -1;
            } else { // 套餐余量充足，重试
                retried++;
            }
        }
        throw new RuntimeException("updateMarketSetting failed, please check it manually");
    }
    
    /**
     * 套餐余量完成部分结算
     *
     * @param customerId
     * @param isSms
     * @param amount
     * @return 成功结算的数量
     */
    private int settlePartWithMarketSetting(Long customerId, boolean isSms, int amount) {
        int retried = 0;
        while (retried < PAYMENT_RETRY) {
            CustomerMarketSetting marketSetting = customerMarketSettingDao.findFirstByCustomerId(customerId);
            Assert.notNull(marketSetting, "customer's marketSetting is not exists");
            int availableNum = isSms ? marketSetting.getSmsTotal() : marketSetting.getMmsTotal();
            if (availableNum < 1) {  // 可支付数量不足，跳出循环
                return 0;
            }
            availableNum = availableNum > amount ? amount : availableNum;
            int result;
            if (isSms) {
                result = customerMarketSettingDao.updateSmsByCustomerId(customerId, -availableNum);
            } else {
                result = customerMarketSettingDao.updateSmsByCustomerId(customerId, -availableNum);
            }
            if (result > 0) {  // 支付成功，跳出循环
                return availableNum;
            }
            retried++;
        }
        throw new RuntimeException("paymentWithCredit failed, please check it manually");
    }
    
    /**
     * 使用信用额度购买消息量
     *
     * @param customerId
     * @param isSms
     * @param amount
     */
    private void purchaseWithCredit(Long customerId, boolean isSms, int amount) {
        if (amount == 0) {
            return;
        }
        int retried = 0;
        BigDecimal cost = isSms ? singleSmsPrice.multiply(new BigDecimal(amount)) : singleMmsPrice.multiply(new BigDecimal(amount));
        while (retried < PAYMENT_RETRY) {
            int result = customerDao.updateCredit(customerId, cost.negate());
            if (result > 0) {  // 信用额度购买成功，跳出循环
                return;
            }
            Optional<Customer> optional = customerDao.findById(customerId);
            Assert.isTrue(optional.isPresent(), "customer is not exists");
            // 若信用额度不足，则无法购买，给出提示
//            ServiceException.isTrue(cost.compareTo(optional.get().getCredit()) <= 0, bundle.getString("credit-not-enough"));
            retried++;
        }
        throw new RuntimeException("purchaseWithCredit failed, please check it manually");
    }
    
}
