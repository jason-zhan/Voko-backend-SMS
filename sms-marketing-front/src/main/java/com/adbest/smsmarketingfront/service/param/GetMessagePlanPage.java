package com.adbest.smsmarketingfront.service.param;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessagePlanStatus;
import com.adbest.smsmarketingfront.service.MessagePlanService;
import com.adbest.smsmarketingfront.util.PageBase;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @see MessagePlan
 * @see MessagePlanService#findByConditions(GetMessagePlanPage)
 */
@Data
public class GetMessagePlanPage extends PageBase {
    /**
     * 定时发送任务状态
     * @see MessagePlanStatus
     */
    private Integer status;
    private Timestamp start;  // 创建时间起始
    private Timestamp end;  // 创建时间结束
    private String keyword;  // 关键词(标题)
}
