package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingfront.service.param.CreateMessagePlan;
import com.adbest.smsmarketingfront.service.param.GetMessagePlanPage;
import com.adbest.smsmarketingfront.service.param.UpdateMessagePlan;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * 消息发送任务前端业务
 * @see MessagePlan
 */
public interface MessagePlanService {
    
    // 新增定时任务
    int create(CreateMessagePlan create);
    
    // 创建立即发送任务
    int createInstant(CreateMessagePlan create);
    
    // 修改定时任务(限定只能修改编辑中状态，无须进行消息结算)
    int update(UpdateMessagePlan update);
    
    // 检查任务是否跨套餐
    boolean checkCrossBeforeCancel(Long id);
    
    // 取消定时任务(无须对任务细节进行校验)
    int cancel(Long id);
    
    // 重启定时任务
    int restart(Long id);
    
    // 删除发送任务(即禁用, 目标：编辑中的任务)
    int delete(Long id);
    
    // 根据id查询发送任务
    MessagePlan findById(Long id);
    
    // 根据条件查询发送任务
    Page<MessagePlan> findByConditions(GetMessagePlanPage getPlanPage);
    
    // 获取消息发送任务状态
    Map<Integer, String> statusMap();
}
