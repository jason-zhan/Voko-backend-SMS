package com.adbest.smsmarketingfront.service.param;

import com.adbest.smsmarketingentity.MmsBill;
import com.adbest.smsmarketingentity.QMmsBill;
import com.adbest.smsmarketingentity.QSmsBill;
import com.adbest.smsmarketingentity.SmsBill;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.QueryDslTools;
import com.querydsl.core.BooleanBuilder;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @see SmsBill
 * @see MmsBill
 */
@Data
public class GetMsgBillPage extends PageBase {
    
    private Timestamp start;  // 开始时间
    private Timestamp end;  // 结束时间
    
    public void fillConditions(BooleanBuilder builder, QSmsBill qSmsBill) {
        builder.and(qSmsBill.customerId.eq(Current.get().getId()));
//        QueryDslTools.betweenNotNull(builder, qSmsBill.time, this.start, this.end);
        QueryDslTools.beforeNotNull(builder, qSmsBill.time, this.end);
        QueryDslTools.afterNotNull(builder, qSmsBill.time, this.start);
    }
    
    public void fillConditions(BooleanBuilder builder, QMmsBill qMmsBill) {
        builder.and(qMmsBill.customerId.eq(Current.get().getId()));
        QueryDslTools.betweenNotNull(builder, qMmsBill.time, this.start, this.end);
    }
    
    @Override
    public String toString() {
        return "GetMsgBillPage{" +
                "start=" + start +
                ", end=" + end +
                ", page=" + page +
                ", size=" + size +
                "}";
    }
}
