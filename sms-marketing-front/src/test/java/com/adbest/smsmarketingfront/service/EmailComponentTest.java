package com.adbest.smsmarketingfront.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailComponentTest {
    
    @Autowired
    EmailComponent emailComponent;
    
    @Test
    public void sendPackageRemainingTip() {
//        emailComponent.sendPackageRemainingTip("midakun1667@dingtalk.com", 100);
        emailComponent.sendMonthlyBill(1L);
    }
}