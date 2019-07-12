package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.entity.vo.InboxMessageVo;
import com.adbest.smsmarketingfront.entity.vo.OutboxMessageVo;
import com.adbest.smsmarketingfront.service.param.GetInboxMessagePage;
import com.adbest.smsmarketingfront.service.param.GetOutboxMessagePage;
import com.adbest.smsmarketingfront.util.EasyTime;
import com.adbest.smsmarketingfront.util.TimeTools;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Map;


@RunWith(SpringRunner.class)
@SpringBootTest
public class MessageRecordServiceTest {
    
    @Autowired
    MessageRecordService messageRecordService;
    
    @Test
    public void testGetOutbox() {
        GetOutboxMessagePage getOutbox = new GetOutboxMessagePage();
        getOutbox.setKeyword("0000000");
//        getOutbox.setHasSent(false);
        getOutbox.setStart(EasyTime.now());
        getOutbox.setEnd(EasyTime.now());
        Page<OutboxMessageVo> messagePage = messageRecordService.findOutboxByConditions(getOutbox);
        System.out.println(messagePage);
    }
    
    @Test
    public void testInbox() {
        GetInboxMessagePage getInbox = new GetInboxMessagePage();
        getInbox.setKeyword("54321");
        Page<InboxMessageVo> messagePage = messageRecordService.findInboxByConditions(getInbox);
        System.out.println(messagePage);
    }
    
    @Test
    public void testBundleStatus() {
        Map<Integer, String> map = messageRecordService.outboxStatusMap();
        System.out.println(map.size());
    }
    
    @Test
    public void delete() {
        messageRecordService.delete(Arrays.asList(1L, 2L, 3L));
    }
    
    @Test
    public void markRead() {
        messageRecordService.markRead(Arrays.asList(601L));
    }
    
    
    @Test
    public void findInboxByConditions() {
        GetInboxMessagePage getInboxPage = new GetInboxMessagePage();
        getInboxPage.setHasRead(true);
//        getInboxPage.setIsSms(false);
//        getInboxPage.setStart(EasyTime.init().addDays(1).stamp());
//        getInboxPage.setEnd(EasyTime.init().addDays(2).stamp());
        getInboxPage.setKeyword("00");
        Page<InboxMessageVo> messageVoPage = messageRecordService.findInboxByConditions(getInboxPage);
        System.out.println(messageVoPage.getTotalElements());
    }
    
    @Test
    public void findOutboxByConditions() {
        GetOutboxMessagePage getOutboxPage = new GetOutboxMessagePage();
//        getOutboxPage.setPlanId(2L);
        getOutboxPage.setKeyword("4");
        Page<OutboxMessageVo> messageVoPage = messageRecordService.findOutboxByConditions(getOutboxPage);
        System.out.println(messageVoPage.getTotalElements());
    }
}