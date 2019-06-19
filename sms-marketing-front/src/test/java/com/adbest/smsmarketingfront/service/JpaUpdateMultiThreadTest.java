package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.util.EasyTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JpaUpdateMultiThreadTest {
    
    @Autowired
    CustomerDao customerDao;
    
    @Test
    public void startTest() {
        for (int i = 0; i < 1000; i++) {
            new Thread(new execJpaThread(i, i % 2 == 0)).start();
        }
        try {
            Thread.sleep(100 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public class execJpaThread implements Runnable {
        
        private int num;  // 线程编号
        private boolean symbol;  // 符号(true: >0)
        
        public execJpaThread(int num, boolean symbol) {
            this.num = num;
            this.symbol = symbol;
        }
        
        @Override
        public void run() {
            long start = EasyTime.nowMillis();
            System.out.printf("%s exec[%s] >%n", num, start);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int result = customerDao.updateCreditByCustomerId(BigDecimal.valueOf(symbol ? 5 : -6), 1L);
            System.out.printf("%s x %s%n", num, result);
            printStatus();
            long end = EasyTime.nowMillis();
            System.out.printf("%s over[%s] <%n", num, end);
        }
        
        private void printStatus() {
//            BigDecimal credit = customerDao.findById(1L).get().getCredit();
//            Assert.isTrue(credit.compareTo(BigDecimal.ZERO) >= 0, "credit < 0");
//            System.out.printf("%s = %s%n", num, credit.toPlainString());
        }
    }
}
