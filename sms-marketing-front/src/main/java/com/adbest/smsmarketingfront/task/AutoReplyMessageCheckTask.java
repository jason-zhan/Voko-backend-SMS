package com.adbest.smsmarketingfront.task;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.MessageReturnCode;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingfront.service.MessageRecordService;
import com.adbest.smsmarketingfront.util.twilio.TwilioUtil;
import com.twilio.rest.api.v2010.account.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class AutoReplyMessageCheckTask {

    @Autowired
    private MessageRecordService messageRecordService;

    @Autowired
    private TwilioUtil twilioUtil;

//    @Scheduled(cron = "50 0/22 * * * ?")
    public void checkAutoReplyMessage(){
        log.info("Check AutoReplyMessage...");
        int size = 200;
        Pageable pageable = null;
        int i = 0;
        int length = 0;
        Message message = null;
        List<MessageRecord> list = null;
        List<MessageRecord> successMsg = new ArrayList();
        List<MessageRecord> failedMsg = new ArrayList();
        do {
            pageable = PageRequest.of(i, size);
            i++;
            list = messageRecordService.findByReturnCodeAndDisableAndPlanIdIsNull(MessageReturnCode.SENT.getValue(), false, pageable);
            if (list.size()==0){return;}
            length = list.size();
            for (MessageRecord mr : list) {
                message = twilioUtil.fetchMessage(mr.getSid());
                switch (message.getStatus()){
                    case QUEUED:;
                    case FAILED:
                        mr.setReturnCode(MessageReturnCode.FAILED.getValue());
                        mr.setStatus(OutboxStatus.FAILED.getValue());
                        failedMsg.add(mr);
                    case SENT:;
                    case DELIVERED:
                        mr.setReturnCode(MessageReturnCode.DELIVERED.getValue());
                        mr.setStatus(OutboxStatus.DELIVERED.getValue());
                        successMsg.add(mr);
                    case UNDELIVERED:
                        mr.setReturnCode(MessageReturnCode.UNDELIVERED.getValue());
                        mr.setStatus(OutboxStatus.UNDELIVERED.getValue());
                        successMsg.add(mr);
                    default:;
                }
            }
            if (successMsg.size()>0){
                messageRecordService.saveAll(successMsg);
                successMsg.clear();
            }
            if (failedMsg.size()>0){
                for (MessageRecord messageRecord : failedMsg) {
                    try {
                        messageRecordService.autoReplyReturn(messageRecord);
                    }catch (Exception e){
                        log.error("SMS failed to return error,{}",e);
                    }
                }
                failedMsg.clear();
            }
            list.clear();
        }while (length==size);
    }
}
