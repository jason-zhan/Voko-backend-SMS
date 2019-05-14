package com.adbest.smsmarketingfront.service.param;

import com.adbest.smsmarketingentity.MmsBill;
import com.adbest.smsmarketingentity.QMmsBill;
import com.adbest.smsmarketingentity.QSmsBill;
import com.adbest.smsmarketingentity.SmsBill;
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
        QueryDslTools.betweenNotNull(builder, qSmsBill.time, this.start, this.end);
    }
    
    public void fillConditions(BooleanBuilder builder, QMmsBill qMmsBill) {
        QueryDslTools.betweenNotNull(builder, qMmsBill.time, this.start, this.end);
    }
}
