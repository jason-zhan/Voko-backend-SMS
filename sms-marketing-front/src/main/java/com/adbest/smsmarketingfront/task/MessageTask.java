package com.adbest.smsmarketingfront.task;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessagePlanStatus;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingentity.QMessagePlan;
import com.adbest.smsmarketingentity.QMessageRecord;
import com.adbest.smsmarketingfront.service.MessageRecordComponent;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.TimeTools;
import com.adbest.smsmarketingfront.util.UrlTools;
import com.adbest.smsmarketingfront.util.twilio.TwilioUtil;
import com.adbest.smsmarketingfront.util.twilio.param.PreSendMsg;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.twilio.rest.api.v2010.account.Message;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息业务 任务合集
 */
@Component
public class MessageTask {
    
    @Autowired
    private TwilioUtil twilioUtil;
    @Value("${twilio.viewFileUrl}")
    private String viewFileUrl;
    @Value("${twilio.planExecTimeDelay}")
    private int planExecTimeDelay;  // 计划等候执行分钟数
    
    @Autowired
    private MessageRecordComponent messageRecordComponent;
    @Autowired
    private Scheduler scheduler;
    @Autowired
    JPAQueryFactory queryFactory;
    
    private static final int singleThreadSendNum = 1000;
    
    // 生成定时发送消息的任务(job)
    @Scheduled(cron = "15 0/10 * * * ?")
    public void generateSendMsgThread() {
        QMessagePlan qMessagePlan = QMessagePlan.messagePlan;
        QMessageRecord qMessageRecord = QMessageRecord.messageRecord;
        // 获取待执行定时发送任务
        List<MessagePlan> planList = getAllTobeExecutingPlan(qMessagePlan);
        // 条件复用
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qMessageRecord.disable.isFalse());
        PageBase pageBase = new PageBase();
        pageBase.setSize(singleThreadSendNum);
        for (MessagePlan plan : planList) {
            while (true) {
                JPAQuery<MessageRecord> jpaQuery = queryFactory.select(qMessageRecord).from(qMessageRecord)
                        .where(builder.and(qMessageRecord.planId.eq(plan.getId())));
                Page<MessageRecord> messagePage = getTobeSendMsgList(jpaQuery, pageBase);
                generateScheduledJob(plan, messagePage);
                if (messagePage.isLast()) {
                    break;
                }
            }
            
        }
        
        
        for (int i = 0; i < 10; i++) {
            Map map = new HashMap();
            map.put("index", i);
            map.put("even", i % 2 == 0);
            map.put("messageRecordComponent", messageRecordComponent);
            JobDetail jobDetail = JobBuilder.newJob(JobTest.class).setJobData(new JobDataMap(map)).withIdentity("" + i, "job-key-").build();
            Trigger trigger = TriggerBuilder.newTrigger().startAt(TimeTools.addSeconds(TimeTools.now(), i * 5)).build();
            try {
                scheduler.scheduleJob(jobDetail, trigger);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
    }
    
    // 异常发送修补线程
    @Scheduled(cron = "0/5 * * * * ?")
    public void repairSendMsg() {
        System.out.println("== do repairSendMsg ==");
        try {
            List<JobExecutionContext> jobs = scheduler.getCurrentlyExecutingJobs();
            for (JobExecutionContext job : jobs) {
                System.out.println("executing job:" + job.getJobDetail().getKey());
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        System.out.println("== leave repairSendMsg ==");
    }
    
    // 发送消息
    private void sendMessage(List<MessageRecord> messageList) {
        for (MessageRecord message : messageList) {
            PreSendMsg preSendMsg = new PreSendMsg(message, UrlTools.getUriList(viewFileUrl, message.getMediaList()));
            Message sentMsg = twilioUtil.sendMessage(preSendMsg);
            message.setSid(sentMsg.getSid());
            message.setStatus(OutboxStatus.SENT.getValue());
            message.setSendTime(TimeTools.now());
            messageRecordComponent.updateMessage(message);
        }
    }
    
    // 获取所有待执行定时发送任务
    private List<MessagePlan> getAllTobeExecutingPlan(QMessagePlan qMessagePlan) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qMessagePlan.status.eq(MessagePlanStatus.SCHEDULING.getValue()))
                .and(qMessagePlan.execTime.before(TimeTools.addMinutes(TimeTools.now(), planExecTimeDelay)));
        List<MessagePlan> planList = queryFactory.select(qMessagePlan).from(qMessagePlan).where(builder).fetch();
        return planList;
    }
    
    // 生成定时发送任务
    private void generateScheduledJob(MessagePlan plan, Page<MessageRecord> messagePage) {
        Map map = new HashMap();
        map.put("messagePage", messagePage);
        JobDetail jobDetail = JobBuilder.newJob(JobTest.class).setJobData(new JobDataMap(map))
                .withIdentity("" + messagePage.getNumber(), plan.getId() + "").build();
        Trigger trigger = TriggerBuilder.newTrigger().startAt(plan.getExecTime()).build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        // TODO 改变计划状态
    }
    
    // 获取一批待发送消息
    private Page<MessageRecord> getTobeSendMsgList(JPAQuery<MessageRecord> jpaQuery, PageBase pageBase) {
        QueryResults<MessageRecord> queryResults = jpaQuery.offset(pageBase.getPage() * pageBase.getSize())
                .limit(pageBase.getSize())
                .fetchResults();
        Page<MessageRecord> messagePage = PageBase.toPageEntity(queryResults, pageBase);
        return messagePage;
    }
}
