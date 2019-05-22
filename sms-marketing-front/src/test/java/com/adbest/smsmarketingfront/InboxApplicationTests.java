package com.adbest.smsmarketingfront;

import com.adbest.smsmarketingentity.Keyword;
import com.adbest.smsmarketingentity.ServiceNumber;
import com.adbest.smsmarketingfront.service.KeywordService;
import com.adbest.smsmarketingfront.service.MessageRecordService;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.twilio.param.InboundMsg;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InboxApplicationTests {

    @Autowired
    private MessageRecordService messageRecordService;

    @Autowired
    private KeywordService keywordService;
    
    @Test
    public void test(){
//        InboundMsg inboundMsg = new InboundMsg();
//        inboundMsg.setMessageSid(UUID.randomUUID().toString());
//        inboundMsg.setBody("优惠");
//        inboundMsg.setFrom("123456");
//        inboundMsg.setTo("654321");
//        messageRecordService.saveInbox(inboundMsg);
    }

}
