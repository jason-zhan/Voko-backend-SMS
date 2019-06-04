package com.adbest.smsmarketingfront.service.param;

import com.adbest.smsmarketingentity.QSmsBill;
import com.adbest.smsmarketingentity.SmsBill;
import com.adbest.smsmarketingfront.service.SmsBillComponent;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.TimeTools;
import com.querydsl.core.BooleanBuilder;

import java.sql.Timestamp;

/**
 * @see SmsBill
 * @see SmsBillComponent#findByConditions(GetSmsBillPage)
 */
public class GetSmsBillPage extends PageBase {
    
    private Timestamp start;  // 生成时间起始点
    private Timestamp end;  // 生成时间结束点
    
    public void fillConditions(BooleanBuilder builder, QSmsBill qSmsBill) {
        if (this.start == null || this.end == null) {
            this.start = TimeTools.addMonth(TimeTools.monthStart(TimeTools.now()), -2);
            this.end = TimeTools.dayEnd(TimeTools.now());
        }
        builder.and(qSmsBill.time.between(this.start, this.end));
    }
}
