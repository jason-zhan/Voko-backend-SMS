package com.adbest.smsmarketingfront;

import com.adbest.smsmarketingfront.service.ContactsGroupService;
import com.adbest.smsmarketingfront.service.KeywordService;
import com.adbest.smsmarketingfront.service.MessageRecordService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InboxApplicationTests {
    
    @Autowired
    private MessageRecordService messageRecordService;
    
    @Autowired
    private KeywordService keywordService;
    
    @Autowired
    private ContactsGroupService contactsGroupService;
    
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
