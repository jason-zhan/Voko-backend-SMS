package com.adbest.smsmarketingfront.task;

import com.adbest.smsmarketingentity.MessagePlanStatus;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingfront.dao.MessagePlanDao;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingfront.util.TimeTools;
import com.adbest.smsmarketingfront.util.UrlTools;
import com.adbest.smsmarketingfront.util.twilio.TwilioUtil;
import com.adbest.smsmarketingfront.util.twilio.param.PreSendMsg;
import com.twilio.rest.api.v2010.account.Message;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

@Slf4j
public class SendMessageJob implements Job {
    
    private MessagePlanDao messagePlanDao;
    private MessageRecordDao messageRecordDao;
    private TwilioUtil twilioUtil;
    private String viewFileUrl;
    
    private final boolean test = true;
    
    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        JobDetail jobDetail = jobContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        // 初始化固件
        if (messagePlanDao == null) {
            messagePlanDao = (MessagePlanDao) jobDataMap.get("messagePlanDao");
            messageRecordDao = (MessageRecordDao) jobDataMap.get("messageRecordDao");
            twilioUtil = (TwilioUtil) jobDataMap.get("twilioUtil");
            viewFileUrl = (String) jobDataMap.get("viewFileUrl");
        }
        // 获取操作参数
        JobKey jobKey = jobDetail.getKey();
        Long planId = Long.valueOf(jobKey.getGroup());
        Integer page = Integer.valueOf(jobKey.getName());
        Integer size = (Integer) jobDataMap.get("size");
        if (planId == null || page == null || size == null) {
            log.info("[ERROR] parameter passing error,  planId=%s, page=%s, size=%s", planId, page, size);
            return;
        }
        if (!test) {
            // 执行
            Page<MessageRecord> messagePage = messageRecordDao.findByPlanIdAndStatusAndDisableIsFalse(planId, OutboxStatus.QUEUE.getValue(),
                    PageRequest.of(page, size, Sort.Direction.ASC, "id"));
            sendMessage(messagePage.getContent());
            // 统计任务完成度
            long queueCount = messageRecordDao.countByPlanIdAndStatusAndDisableIsFalse(planId, OutboxStatus.QUEUE.getValue());
            if (queueCount == 0) {
                messagePlanDao.updateStatusById(planId, MessagePlanStatus.EXECUTION_COMPLETED.getValue());
            }
        } else {
            // 测试代码块
            for (int i = 30; i > 0; i--) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.printf("job:%s:page:%s:%s%n", planId, page, i);
            }
        }
        
    }
    
    // 发送消息
    private void sendMessage(List<MessageRecord> messageList) {
        for (MessageRecord message : messageList) {
            if (test) {
                message.setSid(UUID.randomUUID().toString());
            } else {
                PreSendMsg preSendMsg = new PreSendMsg(message, UrlTools.getUriList(viewFileUrl, message.getMediaList()));
                Message sentMsg = twilioUtil.sendMessage(preSendMsg);
                message.setSid(sentMsg.getSid());
            }
            message.setStatus(OutboxStatus.SENT.getValue());
            message.setSendTime(TimeTools.now());
            messageRecordDao.save(message);
        }
    }
}
