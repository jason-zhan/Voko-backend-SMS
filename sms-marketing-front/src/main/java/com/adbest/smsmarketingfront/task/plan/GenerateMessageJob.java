package com.adbest.smsmarketingfront.task.plan;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessagePlanStatus;
import com.adbest.smsmarketingfront.dao.MessagePlanDao;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingfront.service.MessageComponent;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 生成消息 为发送消息做准备
 */
@Component
@Slf4j
public class GenerateMessageJob implements Job {
    
    private static MessagePlanDao messagePlanDao;
    private static MessageComponent messageComponent;
    private static MessageRecordDao messageRecordDao;
    private static MessagePlanTask messagePlanTask;
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        MessagePlan plan = (MessagePlan) dataMap.get("plan");
        Assert.notNull(plan, "plan is null");
        log.info("run message plan: {}", plan.getId());
        // 当前任务可能已经生成消息
        if (!messageRecordDao.existsByPlanId(plan.getId())) {
            // 执行前校验、生成消息、结算
            plan = messageComponent.validBeforeExec(plan.getId());
        }
        if (plan.getMsgTotal() == 0) {
            // 可发送消息数为0，直接更新任务为完成状态
            messagePlanDao.updateStatusById(plan.getId(), MessagePlanStatus.FINISHED.getValue(), MessagePlanStatus.QUEUING.getValue());
            log.info("update plan({}) to {}({}) for message total is 0. [TASK]",
                    plan.getId(), MessagePlanStatus.FINISHED.getTitle(), MessagePlanStatus.FINISHED.getValue());
            return;
        }
        // 创建消息发送任务
        messagePlanTask.scheduledPlan(plan);
    }
    
    @Autowired
    public void setMessagePlanDao(MessagePlanDao messagePlanDao) {
        GenerateMessageJob.messagePlanDao = messagePlanDao;
    }
    
    @Autowired
    public void setMessageComponent(MessageComponent messageComponent) {
        GenerateMessageJob.messageComponent = messageComponent;
    }
    
    @Autowired
    public void setMessageRecordDao(MessageRecordDao messageRecordDao) {
        GenerateMessageJob.messageRecordDao = messageRecordDao;
    }
    
    @Autowired
    public void setMessagePlanTask(MessagePlanTask messagePlanTask) {
        GenerateMessageJob.messagePlanTask = messagePlanTask;
    }
}
