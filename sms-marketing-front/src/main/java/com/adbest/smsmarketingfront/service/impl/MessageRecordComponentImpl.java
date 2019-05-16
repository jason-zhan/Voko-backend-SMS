package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.MessageReturnCode;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingfront.service.MessageRecordComponent;
import com.adbest.smsmarketingfront.util.twilio.param.StatusCallbackParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageRecordComponentImpl implements MessageRecordComponent {
    
    @Autowired
    MessageRecordDao messageRecordDao;
    
    @Override
    public int createMessage(MessageRecord create) {
        log.info("enter createMessage, param={}", create);
        
        log.info("leave createMessage");
        return 0;
    }
    
    @Override
    public int updateMessage(MessageRecord update) {
        log.info("enter updateMessage, param={}", update);
        
        log.info("leave updateMessage");
        return 0;
    }
    
    @Override
    public int updateMessageStatus(StatusCallbackParam param) {
        log.info("enter updateMessageStatus, param={}", param);
        MessageReturnCode returnCode = null;
        try {
            returnCode = MessageReturnCode.valueOf(param.getMessageStatus());
        } catch (IllegalArgumentException | NullPointerException e) {
            System.out.println(e);
        }
        if (MessageReturnCode.FAILED == returnCode) {
            return messageRecordDao.updateStatusBySid(param.getMessageSid(), OutboxStatus.FAILED.getValue());
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
    public MessageRecord dealWithIncome(MessageRecord income) {
        log.info("enter dealWithIncome, param={}", income);
        
        log.info("leave dealWithIncome");
        return null;
    }
}
