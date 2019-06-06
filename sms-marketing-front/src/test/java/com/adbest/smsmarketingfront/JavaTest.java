package com.adbest.smsmarketingfront;

import com.adbest.smsmarketingfront.util.EasyTime;
import org.junit.Test;

import java.sql.Timestamp;

public class JavaTest {
    
    @Test
    public void testEasyTime(){
//        Timestamp stamp = EasyTime.init().dayStart().addHours(8).addMinutes(30).addSeconds(15).addMonths(3).stamp();
        Timestamp stamp = EasyTime.now();
        System.out.println(stamp.getTime());
    }
}
