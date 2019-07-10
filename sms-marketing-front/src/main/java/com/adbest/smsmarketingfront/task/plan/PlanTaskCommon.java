package com.adbest.smsmarketingfront.task.plan;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.util.EasyTime;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PlanTaskCommon {
    
    /**
     * 是否临近执行期
     *
     * @param plan
     * @return true：是
     */
    public static boolean closeToExecTime(MessagePlan plan) {
        if (plan.getMsgTotal() <= 100000) {
            return (plan.getMsgTotal() < 1000 && plan.getExecTime().before(EasyTime.init().addMinutes(5).stamp())) ||
                    (plan.getMsgTotal() < 10000 && plan.getExecTime().before(EasyTime.init().addMinutes(10).stamp())) ||
                    (plan.getMsgTotal() < 100000 && plan.getExecTime().before(EasyTime.init().addMinutes(20).stamp()));
        }
        return false;
    }
    
    
    /**
     * 创建 生成消息的作业
     *
     * @param plan
     * @return
     */
    public static JobDetail createMessageJob(MessagePlan plan) {
        Map<String, Object> map = new HashMap<>();
        map.put("plan", plan);
        return JobBuilder.newJob(GenerateMessageJob.class)
                .setJobData(new JobDataMap(map))
                .withIdentity(plan.getId().toString(), "CREATE_MSG")
                .build();
    }
    
    
    /**
     * 创建 生成消息的作业触发器
     *
     * @param jobDetail
     * @return
     */
    public static Trigger createMessageTrigger(JobDetail jobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail.getKey())
                .startAt(EasyTime.now())
                .build();
    }
    
    
    /**
     * 创建 发送消息的作业(JobDetail)
     *
     * @param plan
     * @param messagePage
     * @return
     */
    public static JobDetail createSendJob(MessagePlan plan, Page<MessageRecord> messagePage) {
        Map<String, Object> map = new HashMap<>();
        map.put("execTime", plan.getExecTime());
        map.put("messageList", messagePage.getContent());
        return JobBuilder.newJob(SendMessageJob.class)
                .setJobData(new JobDataMap(map))
                .withIdentity(messagePage.getNumber() + "", plan.getId().toString())
                .build();
    }
    
    /**
     * 创建 发送消息的作业触发器
     *
     * @param jobDetail 必须携带执行时间(execTime)
     * @return
     */
    public static Trigger createSendTrigger(JobDetail jobDetail) {
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail.getKey())
                .startAt((Date) jobDataMap.get("execTime"))
                .build();
    }
}
