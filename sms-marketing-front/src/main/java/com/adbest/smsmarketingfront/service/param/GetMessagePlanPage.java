package com.adbest.smsmarketingfront.service.param;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessagePlanStatus;
import com.adbest.smsmarketingentity.QMessagePlan;
import com.adbest.smsmarketingfront.entity.vo.MessagePlanVo;
import com.adbest.smsmarketingfront.service.MessagePlanService;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.QueryDslTools;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;

/**
 * @see MessagePlan
 * @see MessagePlanService#findByConditions(GetMessagePlanPage)
 */
@Data
public class GetMessagePlanPage extends PageBase {
    
    /**
     * 发送任务状态
     *
     * @see MessagePlanStatus
     */
    private Integer status;
    private Timestamp start;  // 创建时间起始
    private Timestamp end;  // 创建时间结束
    private String keyword;  // 关键词(标题)
    
    public void fillConditions(BooleanBuilder builder, QMessagePlan qMessagePlan) {
        builder.and(qMessagePlan.customerId.eq(Current.get().getId()));
        builder.and(qMessagePlan.disable.isFalse());
        QueryDslTools dslTools = new QueryDslTools(builder);
        dslTools.eqNotNull(qMessagePlan.status, this.status);
        dslTools.betweenNotNull(qMessagePlan.createTime, this.start, this.end);
        dslTools.containsNotEmpty(false, qMessagePlan.title, this.keyword);
    }
    
    @Override
    public String toString() {
        return "GetMessagePlanPage{" +
                "status=" + status +
                ", start=" + start +
                ", end=" + end +
                ", keyword='" + keyword +
                ", page=" + page +
                ", size=" + size +
                "}";
    }
}
