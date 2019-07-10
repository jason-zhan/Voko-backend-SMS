package com.adbest.smsmarketingfront.util;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class QuartzTools {
    
    @Autowired
    Scheduler scheduler;
    
    /**
     * 获取所有 JobGroup 的名称列表
     *
     * @return
     */
    public List<String> getGroupNames() {
        try {
            return scheduler.getJobGroupNames();
        } catch (SchedulerException e) {
            throw new RuntimeException("getJobGroupNames exception: ", e);
        }
    }
    
    /**
     * 检测该组是否已存在
     *
     * @param groupName
     * @return
     */
    public boolean groupExists(String groupName) {
        return getJobKeys(groupName).size() > 0;
    }
    
    /**
     * 根据组名获取所有 JobKey
     *
     * @param groupName
     * @return
     */
    public Set<JobKey> getJobKeys(String groupName) {
        try {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals(groupName));
            return jobKeys;
        } catch (SchedulerException e) {
            throw new RuntimeException("getJobKeys exception: ", e);
        }
    }
    
    /**
     * 根据 JobKey 获取 JobDetail
     *
     * @param jobKey
     * @return
     */
    public JobDetail getJobDetail(JobKey jobKey) {
        try {
            return scheduler.getJobDetail(jobKey);
        } catch (SchedulerException e) {
            throw new RuntimeException("getJobDetail exception: ", e);
        }
    }
    
    /**
     * 将任务加入 quartz 容器
     *
     * @param jobDetail
     */
    public void addJob(JobDetail jobDetail) {
        try {
            scheduler.addJob(jobDetail, false, true);
        } catch (SchedulerException e) {
            throw new RuntimeException("addJob exception: ", e);
        }
    }
    
    /**
     * 立即触发任务
     *
     * @param jobKey
     */
    public void triggerInstant(JobKey jobKey) {
        try {
            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            throw new RuntimeException("triggerJob exception: ", e);
        }
    }
    
    /**
     * 关联 Trigger (trigger 须设定 JobKey) 以开启定时任务
     *
     * @param trigger
     */
    public void scheduleJob(Trigger trigger) {
        try {
            scheduler.scheduleJob(trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("scheduleJob exception: ", e);
        }
    }
    
    /**
     * 获取当前 job 总数
     *
     * @return
     */
    public int totalJob() {
        try {
            return scheduler.getJobKeys(GroupMatcher.anyJobGroup()).size();
        } catch (SchedulerException e) {
            throw new RuntimeException("getJobKeys exception: ", e);
        }
    }
    
    /**
     * 获取当前正在执行的 job 总数
     *
     * @return
     */
    public int totalExecutingJob() {
        try {
            return scheduler.getCurrentlyExecutingJobs().size();
        } catch (SchedulerException e) {
            throw new RuntimeException("getCurrentlyExecutingJobs exception: ", e);
        }
    }
    
    /**
     * 如果组不存在则加入 job
     *
     * @param jobDetail 必须携带完整 JobKey(name 和 groupName)
     * @return true:加入成功
     */
    public synchronized boolean addJobIfGroupNone(JobDetail jobDetail) {
        if (groupExists(jobDetail.getKey().getGroup())) {
            return false;
        } else {
            addJob(jobDetail);
            return true;
        }
    }
    
}
