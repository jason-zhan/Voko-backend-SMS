package com.adbest.smsmarketingfront;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.QContacts;
import com.adbest.smsmarketingfront.util.twilio.PreSendMsg;
import com.adbest.smsmarketingfront.util.twilio.TwilioUtil;
import com.querydsl.core.BooleanBuilder;
import com.twilio.rest.api.v2010.account.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsMarketingFrontApplicationTests {
    
    @Autowired
    TwilioUtil twilioUtil;
    

    @Test
    public void contextLoads() {
        QContacts qContacts = QContacts.contacts;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qContacts.customerId.eq(2L));
//        PreSendMsg preSendMsg = new PreSendMsg();
//        MessageRecord record = new MessageRecord();
//        record.setContactsNumber("+8615669052722");
//        record.setCustomerNumber("+16782758458");
//        record.setContent("From twilio, Use Test Credentials -- test message");
//        preSendMsg.setRecord(record);
//        Message message = twilioUtil.sendMessage(preSendMsg);
//        System.out.println(message);
    }

}
