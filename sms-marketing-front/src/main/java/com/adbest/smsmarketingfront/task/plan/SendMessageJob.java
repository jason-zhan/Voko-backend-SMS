package com.adbest.smsmarketingfront.task.plan;

import com.adbest.smsmarketingentity.MessagePlanStatus;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingfront.dao.MessagePlanDao;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingfront.util.TimeTools;
import com.adbest.smsmarketingfront.util.twilio.TwilioUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 发送消息作业
 */
@Component
@Slf4j
public class SendMessageJob implements Job {
    
    private static MessagePlanDao messagePlanDao;
    private static MessageRecordDao messageRecordDao;
    private static TwilioUtil twilioUtil;
    private static String viewFileUrl;
    
    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        JobDetail jobDetail = jobContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        // 获取操作参数
        JobKey jobKey = jobDetail.getKey();
        Long planId = Long.valueOf(jobKey.getGroup());
        Integer page = Integer.valueOf(jobKey.getName());
        List<MessageRecord> messageList = (List<MessageRecord>) jobDataMap.get("messageList");
        if (messageList == null || messageList.size() == 0) {
            System.out.printf("[ERROR] message list is empty,  planId=%s, page=%s, size=%s [task] %n", planId, page, 0);
            return;
        }
        System.out.printf("will execute send message, planId=%s, page=%s, size=%s [task] %n", planId, page, messageList.size());
        // 执行
        sendMessage(messageList, planId, page);
        // 统计任务完成度
        long queueCount = messageRecordDao.countByPlanIdAndStatusAndDisableIsFalse(planId, OutboxStatus.QUEUE.getValue());
        if (queueCount == 0) {
            messagePlanDao.updateStatusById(planId, MessagePlanStatus.EXECUTION_COMPLETED.getValue());
            System.out.printf("complete message plan, planId=%s [task] %n", planId);
        } else {
            System.out.printf("executed send message, planId=%s, page=%s, size=%s [task] %n", planId, page, messageList.size());
        }
    }
    
    // 发送消息
    private void sendMessage(List<MessageRecord> messageList, Long planId, Integer page) {
        List<MessageRecord> sentMessageList = new ArrayList<>();
        for (MessageRecord message : messageList) {
            try {
//                PreSendMsg preSendMsg = new PreSendMsg(message, UrlTools.getUriList(viewFileUrl, message.getMediaList()));
//                Message sentMsg = twilioUtil.sendMessage(preSendMsg);
//                message.setSid(sentMsg.getSid());
                message.setSid(UUID.randomUUID().toString());
                message.setStatus(OutboxStatus.SENT.getValue());
                message.setSendTime(TimeTools.now());
                sentMessageList.add(message);
                System.out.printf("sent(%s) planId=%s page=%s%n", message.getId(), planId, page);
            } catch (Exception e) {
                System.out.printf("sendError(%s) planId=%s page=%s%n", message.getId(), planId, page);
                log.info("sendError: ", e);
            }
        }
        messageRecordDao.saveAll(sentMessageList);
    }
    
    @Autowired
    public void setMessagePlanDao(MessagePlanDao messagePlanDao) {
        SendMessageJob.messagePlanDao = messagePlanDao;
    }
    
    @Autowired
    public void setMessageRecordDao(MessageRecordDao messageRecordDao) {
        SendMessageJob.messageRecordDao = messageRecordDao;
    }
    
    @Autowired
    public void setTwilioUtil(TwilioUtil twilioUtil) {
        SendMessageJob.twilioUtil = twilioUtil;
    }
    
    @Value("${twilio.viewFileUrl}")
    public void setViewFileUrl(String viewFileUrl) {
        SendMessageJob.viewFileUrl = viewFileUrl;
    }
}
