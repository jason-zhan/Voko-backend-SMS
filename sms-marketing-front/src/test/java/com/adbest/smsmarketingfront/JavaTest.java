package com.adbest.smsmarketingfront;

import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingfront.util.EasyTime;
import com.adbest.smsmarketingfront.util.StrSegTools;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    public void testNumberToString() {
        String listStr = StrSegTools.getListStr(Arrays.asList(0, 2.5, 300L));
        System.out.println(listStr);
    }
    
    @Test
    public void regexpTest() {
        String pattern = "(aa)|(bb)|(cc)";
        String target = "There are some strings: aaa bbbaa ccbbb cccaa";
        String result = "There are some strings: a b b c";
        String execed = target.replaceAll(pattern, "");
        System.out.println("result: " + result.equals(execed));
    }
    
    @Test
    public void convertTest() {
        Customer cus = new Customer();
        cus.setId(1L);
        Map<String, Object> map = new HashMap<>();
        map.put("1", cus);
        Customer gCus = (Customer) map.get("1");
        System.out.println("is equal:" + gCus.equals(cus));
    }
    
    @Test
    public void testFlyweight() {
//        Integer lower = 100;
//        Integer lower1 = 100;
//        System.out.println("lower=" + lower + "(" + System.identityHashCode(lower) + ")");
//        System.out.println("lower1=" + lower1 + "(" + System.identityHashCode(lower1) + ")"); //611437735
//        System.out.println("equals: " + (lower == lower1));
        String t1 = "wo ai wo jia";
        String t2 = "wo ai wo jia";
        System.out.printf("t1=%s(%s)%n", t1, t1.hashCode());
        System.out.printf("t2=%s(%s)%n", t2, t2.hashCode());
        System.out.printf("equals: %s%n", t1 == t2);
    }
    
    @Test
    public void testNumber() {
//        long l = 9999999999L;
//        int i = -50000;
//        short s = (short) -50000;  // -32768 ~ 32767
//        byte b = (byte) s;
//        System.out.printf("result=%s%n", s);
//        System.out.printf("calc=%s%n", (i - 32768) % (32768 * 2) + 32768);
//        BigDecimal a = BigDecimal.valueOf(0.1);
//        BigDecimal a = new BigDecimal(0.1);
//        BigDecimal b = BigDecimal.valueOf(0.2);
//        BigDecimal b = new BigDecimal(0.2);
//        BigDecimal c = a.add(b);
//        System.out.printf("result(%s)%n", c);
        BigDecimal a = BigDecimal.valueOf(52.999);
        int b = a.intValue();
        System.out.println(b);
    }
    
    @Test
    public void testTransform() {
        Date date = new Date();
        Object o = date;
        Date d = (Date) o;
        System.out.printf("date(%s)%n", date.hashCode());
        System.out.printf("o(%s)%n", o.hashCode());
        System.out.printf("d(%s)%n", d.hashCode());
        System.out.printf("equals(%s)%n", date == d);
        System.out.println(d);
        
    }
    
}
