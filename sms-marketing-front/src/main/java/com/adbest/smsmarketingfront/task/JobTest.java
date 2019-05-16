package com.adbest.smsmarketingfront.task;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class JobTest implements Job {
    
    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        
        // TODO 执行消息发送任务
        JobDataMap jobDataMap = jobContext.getJobDetail().getJobDataMap();
        System.out.println("job: " + jobDataMap.get("index") + ":" + jobDataMap.get("even") + ":" + jobDataMap.get("messageRecordComponent"));
        for (int i = 30; i > 0; i--) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("job: " + jobDataMap.get("index") + ":" + i);
        }
    }
}
