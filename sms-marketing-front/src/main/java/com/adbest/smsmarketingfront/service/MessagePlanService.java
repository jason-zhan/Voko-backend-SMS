package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingfront.service.param.GetMessagePlanPage;
import org.springframework.data.domain.Page;

/**
 * 消息定时任务前端业务
 * @see MessagePlan
 */
public interface MessagePlanService {
    
    // 新增定时任务
    int create(MessagePlan create);
    
    // 修改定时任务
    int update(MessagePlan update);
    
    // 根据id查询定时任务
    MessagePlan findById(Integer id);
    
    // 根据条件查询定时任务
    Page<MessagePlan> findByConditions(GetMessagePlanPage getPlanPage);
}
