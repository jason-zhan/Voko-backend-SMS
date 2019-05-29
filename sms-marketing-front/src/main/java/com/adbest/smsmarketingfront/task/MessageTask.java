package com.adbest.smsmarketingfront.task;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessagePlanStatus;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingfront.dao.MessagePlanDao;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingfront.util.TimeTools;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 消息业务 任务合集
 */
@Component
@Slf4j
public class MessageTask {
    
    @Value("${twilio.planExecTimeDelay}")
    private int planExecTimeDelay;  // 定时发送距离当前时间最小间隔分钟数
    private static final int repairTimeRange = 1;  // 定时发送修复任务探测时间超出当前时间的分钟数
    
    @Autowired
    MessagePlanDao messagePlanDao;
    @Autowired
    MessageRecordDao messageRecordDao;
    
    @Autowired
    private Scheduler scheduler;
    
    private static final int singleThreadSendNum = 1000;
    
    /**
     * 执行定时发送
     */
//    @Scheduled(fixedRate = 60 * 1000)
    public synchronized void executePlan() {
        log.info("enter executePlan [task]");
        // 获取所有计划中状态的任务
        List<MessagePlan> planList = messagePlanDao.findByStatusAndExecTimeBeforeAndDisableIsFalse(MessagePlanStatus.SCHEDULING.getValue(),
                TimeTools.addMinutes(TimeTools.now(), planExecTimeDelay));
        if (planList.isEmpty()) {
            log.info("leave executePlan for empty list [task]");
            return;
        }
        // 去除正在执行的任务
        checkPlanByGroup(planList);
        if (planList.size() == 0) {
            log.info("leave executePlan for empty list [task]");
            return;
        }
        // 调度任务
        scheduledPlan(planList);
        log.info("leave executePlan [task]");
    }
    
    /**
     * 修补发送消息作业异常
     */
//    @Scheduled(fixedRate = 5000)
//    @Scheduled(initialDelay = 30 * 1000, fixedRate = repairTimeRange * 60 * 1000)
    public synchronized void repairSendMsg() {
        log.info("enter repairSendMsg [task]");
        // 所有队列中状态的任务
        List<MessagePlan> planList = messagePlanDao.findByStatusAndExecTimeBeforeAndDisableIsFalse(MessagePlanStatus.QUEUING.getValue(),
                TimeTools.addMinutes(TimeTools.now(), repairTimeRange));
        // 排除当前正在运行的任务
        checkPlanByGroup(planList);
        if (planList.size() == 0) {
            log.info("leave repairSendMsg for empty list [task]");
            return;
        }
        // 调度任务
        scheduledPlan(planList);
        log.info("leave repairSendMsg [task]");
    }
    
    @Transactional
    public void lockPlanAndMessage(Long planId) {
        // 锁定任务状态
        messagePlanDao.updateStatusById(planId, MessagePlanStatus.QUEUING.getValue());
        // 锁定消息状态
        messageRecordDao.updateStatusByPlanIdAndDisableIsFalse(planId, OutboxStatus.QUEUE.getValue());
    }
    
    private void addJobToScheduler(MessagePlan plan) {
        int page = 0;
        boolean locked = false;
        Page<MessageRecord> messagePage = null;
        do {
            messagePage = messageRecordDao.findByPlanIdAndStatusAndDisableIsFalse(plan.getId(), OutboxStatus.QUEUE.getValue(),
                    PageRequest.of(page, singleThreadSendNum, Sort.Direction.ASC, "id"));
            if (messagePage.isEmpty()) {
                break;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("execTime", plan.getExecTime());
            map.put("messageList", messagePage.getContent());
            JobDetail jobDetail = JobBuilder.newJob(SendMessageJob.class).setJobData(new JobDataMap(map))
                    .withIdentity(page + "", plan.getId().toString()).build();
            try {
                scheduler.addJob(jobDetail, false, true);
                if (!locked) {
                    // 锁定任务与消息
                    lockPlanAndMessage(plan.getId());
                    locked = true;
                }
            } catch (SchedulerException e) {
                log.info("add job err: ", e);
            }
            page++;
        } while (messagePage.hasNext());
    }
    
    private void scheduledPlan(List<MessagePlan> planList) {
        // 立即增加任务到调度器中
        for (MessagePlan plan : planList) {
            addJobToScheduler(plan);
        }
        // 关联触发器
        try {
            for (MessagePlan plan : planList) {
                Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals(plan.getId().toString()));
                for (JobKey jobKey : jobKeys) {
                    JobDataMap jobDataMap = scheduler.getJobDetail(jobKey).getJobDataMap();
                    scheduler.scheduleJob(TriggerBuilder.newTrigger().forJob(jobKey).startAt((Date) jobDataMap.get("execTime")).build());
                }
            }
        } catch (SchedulerException e) {
            log.info("link job with trigger err:", e);
        }
    }
    
    private void checkPlanByGroup(List<MessagePlan> planList) {
        List<String> groupNames = null;
        try {
            groupNames = scheduler.getJobGroupNames();
            System.out.printf("job total=%s%n", scheduler.getJobKeys(GroupMatcher.anyJobGroup()).size());
            System.out.printf("executing job total=%s%n", scheduler.getCurrentlyExecutingJobs().size());
        } catch (SchedulerException e) {
            throw new RuntimeException("get jobGroupNames or jobKeys exception", e);
        }
        for (String name : groupNames) {
            System.out.printf("group [%s] exists", name);
            planList.removeIf(plan -> name.equals(plan.getId().toString()));
        }
    }
}
