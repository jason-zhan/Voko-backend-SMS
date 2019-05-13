package com.adbest.smsmarketingfront.service.param;


import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingfront.service.MessagePlanService;
import lombok.Data;

/**
 * @see MessagePlan
 * @see MessagePlanService#update(UpdateMessagePlan)
 */
@Data
public class UpdateMessagePlan extends CreateMessagePlan{
    
    private Long id;  // 定时任务id
}
