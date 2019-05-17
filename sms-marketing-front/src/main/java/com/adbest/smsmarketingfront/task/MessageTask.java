package com.adbest.smsmarketingfront.task;

import com.adbest.smsmarketingentity.MessagePlan;
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
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

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
    
    private static final int singleThreadSendNum = 1000;
    
    // 生成定时发送消息的任务(job)
//    @Scheduled(cron = "15 0/10 * * * ?")
    public void generateSendMsgThread() {
        log.info("enter generateSendMsgThread [task]");
        // 获取待执行定时发送任务
        List<MessagePlan> planList = messagePlanDao.findByStatusAndExecTimeBeforeAndDisableIsFalse(MessagePlanStatus.SCHEDULING.getValue(),
                TimeTools.addMinutes(TimeTools.now(), planExecTimeDelay));
        for (MessagePlan plan : planList) {
            log.info("do planId: " + plan.getId());
            try {
                // 锁定任务状态
                messagePlanDao.updateStatusById(plan.getId(), MessagePlanStatus.QUEUING.getValue());
                // 锁定消息状态
                messageRecordDao.updateStatusByPlanIdAndDisableIsFalse(plan.getId(), OutboxStatus.QUEUE.getValue());
                // 分发任务
                long count = messageRecordDao.countByPlanIdAndDisableIsFalse(plan.getId());
                int number = 0;
                long totalPage = count % singleThreadSendNum == 0 ? count / singleThreadSendNum : count / singleThreadSendNum + 1;
                while (number < totalPage) {
                    generateScheduledJob(plan, number);
                    number++;
                }
            } catch (Exception e) {
                log.info("message plan generate job shut down", e);
            }
        }
        
        // 测试代码块
        for (int i = 0; i < 20; i++) {
            log.info("do planId: " + i);
            long totalPage = 1 + new Random().nextInt(5);
            int number = 0;
            while (number < totalPage) {
                MessagePlan plan = new MessagePlan();
                plan.setId((long) i);
                plan.setExecTime(TimeTools.addSeconds(TimeTools.now(), i * 5));
                generateScheduledJob(plan, number);
                number++;
            }
        }
        log.info("leave generateSendMsgThread [task]");
    }
    
    // 异常发送修补线程
//    @Scheduled(cron = "0/5 * * * * ?")
//    @Scheduled(cron = "15 5/10 * * * ?")
    public void repairSendMsg() {
        log.info("enter repairSendMsg [task]");
        // 所有队列中的任务
        List<MessagePlan> planList = messagePlanDao.findByStatusAndExecTimeBeforeAndDisableIsFalse(MessagePlanStatus.QUEUING.getValue(),
                TimeTools.addMinutes(TimeTools.now(), repairTimeRange));
        if (planList.size() == 0) {
            return;
        }
        try {
            List<String> groupNames = scheduler.getJobGroupNames();
            // 排除当前正在运行的任务
            for (String name : groupNames) {
                planList.removeIf(plan -> name.equals(plan.getId().toString()));
                System.out.println("group [" + name + "] exists");
            }
            // 分发任务
            for (MessagePlan plan : planList) {
                long count = messageRecordDao.countByPlanIdAndStatusAndDisableIsFalse(plan.getId(), OutboxStatus.QUEUE.getValue());
                int number = 0;
                long totalPage = count % singleThreadSendNum == 0 ? count / singleThreadSendNum : count / singleThreadSendNum + 1;
                while (number < totalPage) {
                    generateScheduledJob(plan, number);
                    number++;
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        log.info("leave repairSendMsg [task]");
    }
    
    /**
     * 生成定时发送任务
     *
     * @param plan   定时发送任务
     * @param number 消息列表当前页
     */
    private void generateScheduledJob(MessagePlan plan, int number) {
        Map map = new HashMap();
        map.put("size", singleThreadSendNum);
        map.put("messagePlanDao", messagePlanDao);
        map.put("messageRecordDao", messageRecordDao);
        map.put("twilioUtil", twilioUtil);
        map.put("viewFileUrl", viewFileUrl);
        JobDetail jobDetail = JobBuilder.newJob(SendMessageJob.class).setJobData(new JobDataMap(map))
                .withIdentity("" + number, plan.getId() + "").build();
        Trigger trigger = TriggerBuilder.newTrigger().startAt(plan.getExecTime()).build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
    
}
