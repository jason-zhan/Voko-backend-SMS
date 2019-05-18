package com.adbest.smsmarketingfront.task;

import com.adbest.smsmarketingentity.MessagePlanStatus;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingfront.dao.MessagePlanDao;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
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
        // 初始化固件
        if (viewFileUrl == null) {
//            messagePlanDao = (MessagePlanDao) jobDataMap.get("messagePlanDao");
//            messageRecordDao = (MessageRecordDao) jobDataMap.get("messageRecordDao");
//            twilioUtil = (TwilioUtil) jobDataMap.get("twilioUtil");
            viewFileUrl = (String) jobDataMap.get("viewFileUrl");
        }
        // 获取操作参数
        JobKey jobKey = jobDetail.getKey();
        Long planId = Long.valueOf(jobKey.getGroup());
        Integer page = Integer.valueOf(jobKey.getName());
        Integer size = (Integer) jobDataMap.get("size");
        List<MessageRecord> messageList = (List<MessageRecord>) jobDataMap.get("messageList");
        if (messageList == null || messageList.size() == 0) {
            System.out.printf("[ERROR] message list is empty,  planId=%s, page=%s, size=%s [task] %n", planId, page, size);
            return;
        }
        System.out.printf("will execute send message, planId=%s, page=%s, size=%s [task] %n", planId, page, size);
        // 执行
        sendMessage(messageList, planId, page);
        // 统计任务完成度
        long queueCount = messageRecordDao.countByPlanIdAndStatusAndDisableIsFalse(planId, OutboxStatus.QUEUE.getValue());
        if (queueCount == 0) {
            messagePlanDao.updateStatusById(planId, MessagePlanStatus.EXECUTION_COMPLETED.getValue());
            System.out.printf("message plan complete, planId=%s [task] %n", planId);
        } else {
            System.out.printf("executed send message, planId=%s, page=%s, size=%s [task] %n", planId, page, size);
        }
        // 测试代码块
//            for (int i = 30; i > 0; i--) {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                System.out.printf("job:%s:page:%s:%s%n", planId, page, i);
//            }
    
    }
    
    // 发送消息
    private void sendMessage(List<MessageRecord> messageList, Long planId, Integer page) {
        for (MessageRecord message : messageList) {
            message.setSid(UUID.randomUUID().toString());
//            PreSendMsg preSendMsg = new PreSendMsg(message, UrlTools.getUriList(viewFileUrl, message.getMediaList()));
//            Message sentMsg = twilioUtil.sendMessage(preSendMsg);
//            messageRecordDao.updateStatusAfterSendMessage(message.getId(), sentMsg.getSid(), OutboxStatus.SENT.getValue());
            messageRecordDao.updateStatusAfterSendMessage(message.getId(), UUID.randomUUID().toString(), OutboxStatus.SENT.getValue());
            System.out.printf("plan:" + planId + ":" + "page:" + page + "msg:" + message.getId() + "%n");
        }
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
