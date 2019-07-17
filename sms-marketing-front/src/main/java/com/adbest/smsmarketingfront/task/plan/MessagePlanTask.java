package com.adbest.smsmarketingfront.task.plan;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessagePlanStatus;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingfront.dao.MessagePlanDao;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingfront.util.EasyTime;
import com.adbest.smsmarketingfront.util.QuartzTools;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 消息业务 任务合集
 */
@Component
@Slf4j
public class MessagePlanTask {
    
    @Value("${twilio.planExecTimeDelay}")
    private int planExecTimeDelay;  // 定时发送距离当前时间最小间隔分钟数
    @Value("${twilio.singleThreadSendNum}")
    private int singleThreadSendNum;
    private static final int repairTimeRange = 1;  // 定时发送修复任务探测时间超出当前时间的分钟数
    
    @Autowired
    private MessagePlanDao messagePlanDao;
    @Autowired
    private MessageRecordDao messageRecordDao;
    
    @Autowired
    private QuartzTools quartzTools;
    
    
    /**
     * 定时扫描并任务
     * 临近执行期的任务将生成消息并加入任务容器
     */
//    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void runPlan() {
        log.info("enter runPlan [TASK]");
        List<MessagePlan> planList = messagePlanDao.findByStatusInAndExecTimeBeforeAndDisableIsFalse(
                Arrays.asList(MessagePlanStatus.SCHEDULING.getValue(), MessagePlanStatus.QUEUING.getValue()),
                EasyTime.init().addMinutes(10).stamp()
        );
        checkPlanByGroup(planList);
        if (planList.isEmpty()) {
            log.info("leave runPlan for empty plan list [TASK]");
            return;
        }
        for (MessagePlan plan : planList) {
            // 是否临近执行期
            if (!PlanTaskCommon.closeToExecTime(plan)) {
                continue;
            }
            JobDetail messageJob = PlanTaskCommon.createMessageJob(plan);
            // 将任务加入容器
            if (!quartzTools.addJobIfGroupNone(messageJob)) {
                continue;
            }
            // 锁定任务，使得不可被修改
            messagePlanDao.updateStatusById(plan.getId(), MessagePlanStatus.QUEUING.getValue(), MessagePlanStatus.SCHEDULING.getValue());
            // 生成消息的作业加入容器中，异步执行
            quartzTools.scheduleJob(PlanTaskCommon.createMessageTrigger(messageJob));
        }
        log.info("leave runPlan [TASK]");
    }
    
    /**
     * 检测中断任务并继续执行
     */
//    @Scheduled(initialDelay = 15 * 1000, fixedDelay = repairTimeRange * 60 * 1000)
    public void continuePlan() {
        log.info("enter continuePlan [task]");
        // 所有执行中状态的任务
        List<MessagePlan> planList = messagePlanDao.findByStatusInAndExecTimeBeforeAndDisableIsFalse(Arrays.asList(MessagePlanStatus.EXECUTING.getValue()),
                EasyTime.now());
        // 排除当前正在运行的任务（避免与正常执行作业重复）
        checkPlanByGroup(planList);
        if (planList.size() == 0) {
            log.info("leave continuePlan for empty plan list [task]");
            return;
        }
        // 产生任务并加入quartz容器
        for (MessagePlan plan : planList) {
            Page<MessageRecord> messagePage = null;
            int page = 0;
            do {
                messagePage = getQueueUsableMessagePage(plan.getId(), page);
                if (messagePage.isEmpty()) {
                    log.info("break for empty message list [task]");
                    break;
                }
                JobDetail jobDetail = PlanTaskCommon.createSendJob(plan, messagePage);
                quartzTools.addJob(jobDetail);
                page++;
            } while (messagePage.hasNext());
            // 关联触发器
            setTriggers(plan);
        }
        log.info("leave continuePlan [task]");
    }
    
    /**
     * 完成消息发送任务
     * 1.对于所有消息的状态回执已达到最终状态的任务，直接归档完成
     * 2.对于部分消息的状态回执未达到最终状态的任务，主动查询更新消息状态，并视条件归档完成
     */
    @Scheduled(initialDelay = 1 * 1000, fixedDelay = 10 * 60 * 1000)
    public void finishPlan() {
        log.info("enter finishPlan [TASK]");
        List<MessagePlan> planList = messagePlanDao.findByStatusAndDisableIsFalse(MessagePlanStatus.EXECUTION_COMPLETED.getValue());
        checkPlanByGroup(planList);
        if (planList.size() == 0) {
            log.info("leave finishPlan for empty plan list [TASK]");
            return;
        }
        for (MessagePlan plan : planList) {
            JobDetail fetchMsgJob = PlanTaskCommon.createFetchMsgJob(plan);
            if (!quartzTools.addJobIfGroupNone(fetchMsgJob)) {
                continue;
            }
            quartzTools.scheduleJob(PlanTaskCommon.createFetchMsgTrigger(fetchMsgJob));
        }
        log.info("leave finishPlan [TASK]");
    }
    
    // 设定触发条件
    private void setTriggers(MessagePlan plan) {
        Set<JobKey> jobKeys = quartzTools.getJobKeys(plan.getId().toString());
        for (JobKey jobKey : jobKeys) {
            quartzTools.scheduleJob(PlanTaskCommon.createSendTrigger(quartzTools.getJobDetail(jobKey)));
        }
    }
    
    // 检测并打印当前容器中任务和组信息
    private void checkPlanByGroup(List<MessagePlan> planList) {
        List<String> groupNames = null;
        groupNames = quartzTools.getGroupNames();
        System.out.printf("job[all] total=%s%n", quartzTools.totalJob());
        System.out.printf("job[executing] total=%s%n", quartzTools.totalExecutingJob());
        for (String name : groupNames) {
            System.out.printf("group [%s] exists %n", name);
            planList.removeIf(plan -> name.equals(plan.getId().toString()));
        }
    }
    
    // 获取队列中的可用消息列表
    private Page<MessageRecord> getQueueUsableMessagePage(Long planId, int page) {
        return messageRecordDao.findByPlanIdAndStatusAndDisableIsFalse(planId, OutboxStatus.QUEUE.getValue(), PageRequest.of(page, singleThreadSendNum));
    }
    
    // 分配任务到quartz容器
    @Async
    public void scheduledPlan(MessagePlan plan) {
        boolean debug = false;
        // 锁定消息
        messageRecordDao.updateStatusByPlanIdAndDisableIsFalse(plan.getId(), OutboxStatus.QUEUE.getValue());
        Page<MessageRecord> messagePage = getQueueUsableMessagePage(plan.getId(), 0);
        // 任务排他性加入容器
        if (!quartzTools.addJobIfGroupNone(PlanTaskCommon.createSendJob(plan, messagePage))) {
            return;
        }
        // 更新任务状态
        messagePlanDao.updateStatusById(plan.getId(), MessagePlanStatus.EXECUTING.getValue(), MessagePlanStatus.QUEUING.getValue());
        if (debug) {
            log.info("== break for debug ==");
            return;
        }
        // 后续任务加入容器
        while (messagePage.hasNext()) {
            messagePage = getQueueUsableMessagePage(plan.getId(), messagePage.nextPageable().getPageNumber());
            quartzTools.addJob(PlanTaskCommon.createSendJob(plan, messagePage));
        }
        // 关联触发器
        setTriggers(plan);
    }
    
}
