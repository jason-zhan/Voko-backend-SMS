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
import org.springframework.core.env.Environment;
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
    @Scheduled(fixedRate = 60 * 1000)
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
        // 产生任务并加入quartz容器
        for (MessagePlan plan : planList) {
            // 锁定消息
            messageRecordDao.updateStatusByPlanIdAndDisableIsFalse(plan.getId(), OutboxStatus.QUEUE.getValue());
            Page<MessageRecord> messagePage = null;
            int page = 0;
            boolean locked = false;
            do {
                messagePage = messageRecordDao.findByPlanIdAndStatusAndDisableIsFalse(plan.getId(), OutboxStatus.QUEUE.getValue(), PageRequest.of(page, singleThreadSendNum));
                if (messagePage.isEmpty()) {
                    log.info("break for empty message list");
                    break;
                }
                JobDetail jobDetail = generateJob(plan, messagePage);
                try {
                    scheduler.addJob(jobDetail, false, true);
                } catch (SchedulerException e) {
                    log.info("add job err: ", e);
                }
                if (!locked) {
                    // 锁定任务
                    messagePlanDao.updateStatusById(plan.getId(), MessagePlanStatus.QUEUING.getValue());
                    locked = true;
                }
                page++;
            } while (messagePage.hasNext());
        }
        // 设定各任务触发条件
        setTriggers(planList);
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
                TimeTools.addMinutes(TimeTools.now(), repairTimeRange));
        // 排除当前正在运行的任务
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
                messagePage = messageRecordDao.findByPlanIdAndStatusAndDisableIsFalse(plan.getId(), OutboxStatus.QUEUE.getValue(), PageRequest.of(page, singleThreadSendNum));
                if (messagePage.isEmpty()) {
                    log.info("break for empty message list");
                    break;
                }
                JobDetail jobDetail = generateJob(plan, messagePage);
                try {
                    scheduler.addJob(jobDetail, false, true);
                } catch (SchedulerException e) {
                    log.info("add job err: ", e);
                }
                page++;
            } while (messagePage.hasNext());
        }
        // 设定各任务触发条件
        setTriggers(planList);
        
        log.info("leave repairSendMsg [task]");
    }
    
    // 设定触发条件
    private void setTriggers(List<MessagePlan> planList) {
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
            System.out.printf("group [%s] exists %n", name);
            planList.removeIf(plan -> name.equals(plan.getId().toString()));
        }
    }
    
    private JobDetail generateJob(MessagePlan plan, Page<MessageRecord> messagePage) {
        Map<String, Object> map = new HashMap<>();
        map.put("execTime", plan.getExecTime());
        map.put("messageList", messagePage.getContent());
        return JobBuilder.newJob(SendMessageJob.class).setJobData(new JobDataMap(map))
                .withIdentity(messagePage.getNumber() + "", plan.getId().toString()).build();
    }
}
