package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingfront.service.param.CreateMessagePlan;
import com.adbest.smsmarketingfront.service.param.GetMessagePlanPage;
import com.adbest.smsmarketingfront.service.param.UpdateMessagePlan;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * 消息定时任务前端业务
 * @see MessagePlan
 */
public interface MessagePlanService {
    
    // 新增定时任务
    int create(CreateMessagePlan create);
    
    // 修改定时任务（编辑中状态）
    int update(UpdateMessagePlan update);
    
    // 取消定时任务
    int cancel(Long id);
    
    // 重启定时任务
    int restart(Long id);
    
    // 根据id查询定时任务
    MessagePlan findById(Long id);
    
    // 根据条件查询定时任务
    Page<MessagePlan> findByConditions(GetMessagePlanPage getPlanPage);
    
    // 获取定时任务状态
    Map<Integer, String> statusMap();
}
