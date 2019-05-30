package com.adbest.smsmarketingfront.task.plan;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessageRecord;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.Map;

public class PlanTaskCommon {
    
    
    /**
     * 产生任务细节(JobDetail)
     *
     * @param plan
     * @param messagePage
     * @return
     */
    public static JobDetail generateJob(MessagePlan plan, Page<MessageRecord> messagePage) {
        Map<String, Object> map = new HashMap<>();
        map.put("execTime", plan.getExecTime());
        map.put("messageList", messagePage.getContent());
        return JobBuilder.newJob(SendMessageJob.class).setJobData(new JobDataMap(map))
                .withIdentity(messagePage.getNumber() + "", plan.getId().toString()).build();
    }
}
