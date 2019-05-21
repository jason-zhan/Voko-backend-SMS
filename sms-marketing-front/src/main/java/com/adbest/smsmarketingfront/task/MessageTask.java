package com.adbest.smsmarketingfront.task;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessagePlanStatus;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingfront.dao.MessagePlanDao;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingfront.service.SmsBillComponent;
import com.adbest.smsmarketingfront.util.TimeTools;
import com.adbest.smsmarketingfront.util.twilio.TwilioUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息业务 任务合集
 */
@Component
@Slf4j
public class MessageTask {
    
    @Autowired
    private TwilioUtil twilioUtil;
    @Value("${twilio.viewFileUrl}")
    private String viewFileUrl;
    @Value("${twilio.planExecTimeDelay}")
    private int planExecTimeDelay;  // 定时发送等候执行分钟数
    private static final int repairTimeRange = 5;  // 修复定时发送预先探测分钟数
    
    @Autowired
    MessagePlanDao messagePlanDao;
    @Autowired
    MessageRecordDao messageRecordDao;
    
    @Autowired
    private Scheduler scheduler;
    
    @Autowired
    private SmsBillComponent smsBillComponent;
    
    private static final int singleThreadSendNum = 1000;
    
    /**
     * 分发定时发送消息作业(job)
     */
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void distributeSendMsgJob() {
        log.info("enter generateSendMsgThread [task]");
        // 获取待执行定时发送任务
        List<MessagePlan> planList = messagePlanDao.findByStatusAndExecTimeBeforeAndDisableIsFalse(MessagePlanStatus.SCHEDULING.getValue(),
                TimeTools.addMinutes(TimeTools.now(), planExecTimeDelay));
        for (MessagePlan plan : planList) {
            log.info("do planId: " + plan.getId());
            // 锁定任务和消息
            lockPlanAndMessage(plan.getId());
            // 分发任务
            distributeJob(plan);
        }
        
        // 测试代码块
//        for (int i = 0; i < 20; i++) {
//            log.info("do planId: " + i);
//            long totalPage = 1 + new Random().nextInt(5);
//            int number = 0;
//            while (number < totalPage) {
//                MessagePlan plan = new MessagePlan();
//                plan.setId((long) i);
//                plan.setExecTime(TimeTools.addSeconds(TimeTools.now(), i * 5));
//                generateScheduledJob(plan, number);
//                number++;
//            }
//        }
        log.info("leave generateSendMsgThread [task]");
    }
    
    /**
     * 修补发送消息作业异常
     */
//    @Scheduled(cron = "0/5 * * * * ?")
    @Scheduled(initialDelay = 5 * 60 * 1000, fixedRate = 10 * 60 * 1000)
    public void repairSendMsg() {
        log.info("enter repairSendMsg [task]");
        // 所有队列中的任务
        List<MessagePlan> planList = messagePlanDao.findByStatusAndExecTimeBeforeAndDisableIsFalse(MessagePlanStatus.QUEUING.getValue(),
                TimeTools.addMinutes(TimeTools.now(), repairTimeRange));
        if (planList.size() == 0) {
            log.info("leave repairSendMsg 0 [task]");
            return;
        }
        List<String> groupNames = null;
        try {
            groupNames = scheduler.getJobGroupNames();
        } catch (SchedulerException e) {
            throw new RuntimeException("getJobGroupNames() exception", e);
        }
        // 排除当前正在运行的任务
        for (String name : groupNames) {
            planList.removeIf(plan -> name.equals(plan.getId().toString()));
            System.out.println("group [" + name + "] exists");
        }
        // 分发任务
        for (MessagePlan plan : planList) {
            distributeJob(plan);
        }
        log.info("leave repairSendMsg [task]");
    }
    
    // 分发定时发送作业
    private void distributeJob(MessagePlan plan) {
        int page = 0;
        Page<MessageRecord> messagePage = null;
        List<JobDetail> jobDetailList = new ArrayList<>();
        do {
            messagePage = messageRecordDao.findByPlanIdAndStatusAndDisableIsFalse(plan.getId(), OutboxStatus.QUEUE.getValue(),
                    PageRequest.of(page, singleThreadSendNum, Sort.Direction.ASC, "id"));
            if (messagePage.isEmpty()) {
                return;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("messageList", messagePage.getContent());
            map.put("size", singleThreadSendNum);
            JobDetail jobDetail = JobBuilder.newJob(SendMessageJob.class).setJobData(new JobDataMap(map))
                    .withIdentity("" + page, plan.getId() + "").build();
            jobDetailList.add(jobDetail);
            page++;
        } while (messagePage.hasNext());
        // 装配数据后，再分配任务，避免并发
        for (JobDetail jobDetail : jobDetailList) {
            Trigger trigger = TriggerBuilder.newTrigger().startAt(plan.getExecTime()).build();
            try {
                scheduler.scheduleJob(jobDetail, trigger);
            } catch (SchedulerException e) {
                log.info("distributeJob [SchedulerException]", e);
            }
        }
    }
    
    @Transactional
    public void lockPlanAndMessage(Long planId) {
        // 锁定任务状态
        messagePlanDao.updateStatusById(planId, MessagePlanStatus.QUEUING.getValue());
        // 锁定消息状态
        messageRecordDao.updateStatusByPlanIdAndDisableIsFalse(planId, OutboxStatus.QUEUE.getValue());
    }
    
}
