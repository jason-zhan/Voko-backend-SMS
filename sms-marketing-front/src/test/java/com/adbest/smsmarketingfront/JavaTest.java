package com.adbest.smsmarketingfront;

import com.adbest.smsmarketingfront.util.EasyTime;
import com.adbest.smsmarketingfront.util.StrSegTools;
import org.junit.Test;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Arrays;

public class JavaTest {
    
    @Test
    public void testEasyTime() {
//        Timestamp stamp = EasyTime.init().dayStart().addHours(8).addMinutes(30).addSeconds(15).addMonths(3).stamp();
        Timestamp stamp = EasyTime.now();
        System.out.println(stamp.getTime());
    }
    
    @Test
    public void testDoWhile() {
        int r = 0;
        do {
            System.out.printf("r=%s%n", r);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (r == 9) {
                continue;
            }
            r++;
        } while (r < 10);
    }
    
    @Test
    public void testNumberToString(){
        String listStr = StrSegTools.getNumberListStr(Arrays.asList(0, 2.5, 300L));
        System.out.println(listStr);
    }
}
