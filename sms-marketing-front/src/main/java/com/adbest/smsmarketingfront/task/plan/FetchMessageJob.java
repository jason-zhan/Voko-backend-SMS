package com.adbest.smsmarketingfront.task.plan;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingfront.service.MessageComponent;
import com.adbest.smsmarketingfront.util.twilio.TwilioUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class FetchMessageJob implements Job {
    
    private static MessageRecordDao messageRecordDao;
    private static MessageComponent messageComponent;
    
    private static TwilioUtil twilioUtil;
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        MessagePlan plan = (MessagePlan) jobDataMap.get("plan");
        Page<MessageRecord> messagePage = null;
        int page = 0;
        log.info("will fetch message plan: {}", plan.getId());
        do {
            messagePage = messageRecordDao.findByPlanIdAndStatusAndDisableIsFalse(plan.getId(), OutboxStatus.SENT.getValue(), PageRequest.of(page, 1000));
            for (MessageRecord message : messagePage.getContent()) {
                messageComponent.updateMessageStatus(message.getSid(), "delivered");
//                Message fetchedMsg = twilioUtil.fetchMessage(message.getSid());
//                if (fetchedMsg != null) {
//                    messageComponent.updateMessageStatus(message.getSid(), fetchedMsg.getStatus().toString());
//                }
            }
            page++;
        } while (messagePage.hasNext());
        // 验证完成任务
        messageComponent.validAndFinishPlan(plan);
    }
    
    @Autowired
    public void setMessageRecordDao(MessageRecordDao messageRecordDao) {
        FetchMessageJob.messageRecordDao = messageRecordDao;
    }
    
    @Autowired
    public void setMessageComponent(MessageComponent messageComponent) {
        FetchMessageJob.messageComponent = messageComponent;
    }
    
    @Autowired
    public void setTwilioUtil(TwilioUtil twilioUtil) {
        FetchMessageJob.twilioUtil = twilioUtil;
    }
    
}
