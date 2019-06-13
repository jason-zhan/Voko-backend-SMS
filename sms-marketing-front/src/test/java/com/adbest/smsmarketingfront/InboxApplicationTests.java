package com.adbest.smsmarketingfront;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.service.ContactsGroupService;
import com.adbest.smsmarketingfront.service.KeywordService;
import com.adbest.smsmarketingfront.service.MessageRecordService;
import com.adbest.smsmarketingfront.util.UrlTools;
import com.adbest.smsmarketingfront.util.twilio.TwilioUtil;
import com.adbest.smsmarketingfront.util.twilio.param.InboundMsg;
import com.adbest.smsmarketingfront.util.twilio.param.PreSendMsg;
import com.twilio.base.ResourceSet;
import com.twilio.rest.api.v2010.account.IncomingPhoneNumber;
import com.twilio.rest.api.v2010.account.availablephonenumbercountry.Local;
import com.twilio.rest.api.v2010.account.availablephonenumbercountry.TollFree;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
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
    
    @Autowired
    private TwilioUtil twilioUtil;
    
    @Test
    public void test() {
//        InboundMsg inboundMsg = new InboundMsg();
//        inboundMsg.setMessageSid(UUID.randomUUID().toString());
//        inboundMsg.setBody("优惠");
//        inboundMsg.setFrom("123456");
//        inboundMsg.setTo("+123456789");
//        messageRecordService.saveInbox(inboundMsg);
        MessageRecord send = new MessageRecord();
        send.setCustomerNumber("+12565768219");
        send.setContent("qq00test..");
        send.setSms(true);
        send.setContactsNumber("");
        send.setInbox(false);
        send.setDisable(false);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        send.setSendTime(timestamp);
        send.setExpectedSendTime(timestamp);
        send.setStatus(OutboxStatus.SENT.getValue());
        PreSendMsg preSendMsg = new PreSendMsg(send);
//        twilioUtil.sendMessage(preSendMsg);
    }
    
}
