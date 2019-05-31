package com.adbest.smsmarketingfront.task.plan;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessageRecord;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PlanTaskCommon {
    
    
    /**
     * 生成任务细节(JobDetail)
     *
     * @param plan
     * @param messagePage
     * @return
     */
    public static JobDetail generateJob(MessagePlan plan, Page<MessageRecord> messagePage) {
        Map<String, Object> map = new HashMap<>();
        map.put("execTime", plan.getExecTime());
        map.put("messageList", messagePage.getContent());
        return JobBuilder.newJob(SendMessageJob.class)
                .setJobData(new JobDataMap(map))
                .withIdentity(messagePage.getNumber() + "", plan.getId().toString())
                .build();
    }
    
    /**
     * 生成触发器
     *
     * @param jobDetail 必须携带执行时间(execTime)
     * @return
     */
    public static Trigger generateTrigger(JobDetail jobDetail) {
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail.getKey())
                .startAt((Date) jobDataMap.get("execTime"))
                .build();
    }
}
