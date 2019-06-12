package com.adbest.smsmarketingfront.service.param;

import com.adbest.smsmarketingentity.QSmsBill;
import com.adbest.smsmarketingentity.SmsBill;
import com.adbest.smsmarketingfront.service.SmsBillComponent;
import com.adbest.smsmarketingfront.util.EasyTime;
import com.adbest.smsmarketingfront.util.PageBase;
import com.querydsl.core.BooleanBuilder;

import java.sql.Timestamp;

/**
 * @see SmsBill
 * @see SmsBillComponent#findByConditionsToExcel(GetSmsBillPage)
 */
public class GetSmsBillPage extends PageBase {
    
    private Timestamp start;  // 生成时间起始点
    private Timestamp end;  // 生成时间结束点
    
    public void fillConditions(BooleanBuilder builder, QSmsBill qSmsBill) {
        if (this.start == null || this.end == null) {
            this.start = EasyTime.init().monthStart().addMonths(-2).stamp();
            this.end = EasyTime.init().dayEnd().stamp();
        }
        builder.and(qSmsBill.time.between(this.start, this.end));
    }
    
    @Override
    public String toString() {
        return "GetSmsBillPage{" +
                "start=" + start +
                ", end=" + end +
                ", page=" + page +
                ", size=" + size +
                "}";
    }
}
