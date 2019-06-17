package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingfront.entity.vo.OutboxMessageVo;
import com.adbest.smsmarketingfront.service.param.GetInboxMessagePage;
import com.adbest.smsmarketingfront.service.param.GetOutboxMessagePage;
import com.adbest.smsmarketingfront.util.TimeTools;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;


@RunWith(SpringRunner.class)
@SpringBootTest
public class MessageRecordServiceTest {
    
    @Autowired
    MessageRecordService messageRecordService;
    
    @Test
    public void testGetOutbox(){
        GetOutboxMessagePage getOutbox = new GetOutboxMessagePage();
        getOutbox.setKeyword("0000000");
        getOutbox.setHasSent(false);
        getOutbox.setStart(TimeTools.now());
        getOutbox.setEnd(TimeTools.now());
        Page<OutboxMessageVo> messagePage = messageRecordService.findOutboxByConditions(getOutbox);
        System.out.println(messagePage);
    }
    
    @Test
    public void testInbox(){
        GetInboxMessagePage getInbox = new GetInboxMessagePage();
        getInbox.setKeyword("54321");
        Page<OutboxMessageVo> messagePage = messageRecordService.findInboxByConditions(getInbox);
        System.out.println(messagePage);
    }
    
    @Test
    public void testBundleStatus(){
        Map<Integer, String> map = messageRecordService.outboxStatusMap();
        System.out.println(map.size());
    }
    
}