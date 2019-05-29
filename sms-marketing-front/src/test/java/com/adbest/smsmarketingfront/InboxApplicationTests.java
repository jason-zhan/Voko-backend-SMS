package com.adbest.smsmarketingfront;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.service.ContactsGroupService;
import com.adbest.smsmarketingfront.service.KeywordService;
import com.adbest.smsmarketingfront.service.MessageRecordService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InboxApplicationTests {
    
    @Autowired
    private MessageRecordService messageRecordService;
    
    @Autowired
    private KeywordService keywordService;
    
    @Autowired
    private ContactsGroupService contactsGroupService;

    @Autowired
    private RedisTemplate redisTemplate;
    
    @Test
    public void test() {
//        InboundMsg inboundMsg = new InboundMsg();
//        inboundMsg.setMessageSid(UUID.randomUUID().toString());
//        inboundMsg.setBody("优惠");
//        inboundMsg.setFrom("123456");
//        inboundMsg.setTo("654321");
//        messageRecordService.saveInbox(inboundMsg);

    }

}
