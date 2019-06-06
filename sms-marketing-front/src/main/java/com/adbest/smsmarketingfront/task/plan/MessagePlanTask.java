package com.adbest.smsmarketingfront.task.plan;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessagePlanStatus;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingfront.dao.MessagePlanDao;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingfront.util.EasyTime;
import com.adbest.smsmarketingfront.util.QuartzTools;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 消息业务 任务合集
 */
@Component
@Slf4j
public class MessagePlanTask {
    
    @Value("${twilio.planExecTimeDelay}")
    private int planExecTimeDelay;  // 定时发送距离当前时间最小间隔分钟数
    @Value("${twilio.singleThreadSendNum}")
    private int singleThreadSendNum;
    private static final int repairTimeRange = 1;  // 定时发送修复任务探测时间超出当前时间的分钟数
    
    @Autowired
    private MessagePlanDao messagePlanDao;
    @Autowired
    private MessageRecordDao messageRecordDao;
    
    @Autowired
    private QuartzTools quartzTools;
    
    
    /**
     * 执行定时发送
     */
    @Scheduled(fixedDelay = 60 * 1000)
    public synchronized void executePlan() {
        log.info("enter executePlan [task]");
        // 获取所有计划中状态的任务
        List<MessagePlan> planList = messagePlanDao.findByStatusAndExecTimeBeforeAndDisableIsFalse(MessagePlanStatus.SCHEDULING.getValue(),
                EasyTime.init().addMinutes(planExecTimeDelay).stamp());
        if (planList.isEmpty()) {
            log.info("leave executePlan for empty list [task]");
            return;
        }
        // 循环分配任务到quartz容器
        for (MessagePlan plan : planList) {
            scheduledPlan(plan);
        }
        log.info("leave executePlan [task]");
    }
    
    /**
     * 修补发送消息作业异常
     */
    @Scheduled(initialDelay = 30 * 1000, fixedRate = repairTimeRange * 60 * 1000)
    public synchronized void repairSendMsg() {
        log.info("enter repairSendMsg [task]");
        // 所有队列中状态的任务
        List<MessagePlan> planList = messagePlanDao.findByStatusAndExecTimeBeforeAndDisableIsFalse(MessagePlanStatus.QUEUING.getValue(),
                EasyTime.init().addMinutes(repairTimeRange).stamp());
        // 排除当前正在运行的任务（避免与正常执行作业重复）
        checkPlanByGroup(planList);
        if (planList.size() == 0) {
            log.info("leave repairSendMsg for empty list [task]");
            return;
        }
        // 产生任务并加入quartz容器
        for (MessagePlan plan : planList) {
            Page<MessageRecord> messagePage = null;
            int page = 0;
            do {
                messagePage = getQueueUsableMessagePage(plan.getId(), page);
                if (messagePage.isEmpty()) {
                    log.info("break for empty message list");
                    break;
                }
                JobDetail jobDetail = PlanTaskCommon.generateJob(plan, messagePage);
                quartzTools.addJob(jobDetail);
                page++;
            } while (messagePage.hasNext());
            // 关联触发器
            setTriggers(plan);
        }
        log.info("leave repairSendMsg [task]");
    }
    
    // 设定触发条件
    private void setTriggers(MessagePlan plan) {
        Set<JobKey> jobKeys = quartzTools.getJobKeys(plan.getId().toString());
        for (JobKey jobKey : jobKeys) {
            quartzTools.scheduleJob(PlanTaskCommon.generateTrigger(quartzTools.getJobDetail(jobKey)));
        }
    }
    
    // 检测并打印当前容器中任务和组信息
    private void checkPlanByGroup(List<MessagePlan> planList) {
        List<String> groupNames = null;
        groupNames = quartzTools.getGroupNames();
        System.out.printf("job[all] total=%s%n", quartzTools.totalJob());
        System.out.printf("job[executing] total=%s%n", quartzTools.totalExecutingJob());
        for (String name : groupNames) {
            System.out.printf("group [%s] exists %n", name);
            planList.removeIf(plan -> name.equals(plan.getId().toString()));
        }
    }
    
    // 获取队列中的可用消息列表
    private Page<MessageRecord> getQueueUsableMessagePage(Long planId, int page) {
        return messageRecordDao.findByPlanIdAndStatusAndDisableIsFalse(planId, OutboxStatus.QUEUE.getValue(), PageRequest.of(page, singleThreadSendNum));
    }
    
    // 分配任务到quartz容器
    @Async
    public void scheduledPlan(MessagePlan plan) {
        // 锁定消息
        messageRecordDao.updateStatusByPlanIdAndDisableIsFalse(plan.getId(), OutboxStatus.QUEUE.getValue());
        Page<MessageRecord> messagePage = getQueueUsableMessagePage(plan.getId(), 0);
        // 任务排他性加入容器
        if (!quartzTools.addJobIfGroupNone(PlanTaskCommon.generateJob(plan, messagePage))) {
            return;
        }
        // 锁定任务
        messagePlanDao.updateStatusById(plan.getId(), MessagePlanStatus.QUEUING.getValue());
        // 后续任务加入容器
        while (messagePage.hasNext()) {
            messagePage = getQueueUsableMessagePage(plan.getId(), messagePage.nextPageable().getPageNumber());
            quartzTools.addJob(PlanTaskCommon.generateJob(plan, messagePage));
        }
        // 关联触发器
        setTriggers(plan);
    }
    
}
