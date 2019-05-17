package com.adbest.smsmarketingfront;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.task.MessageTask;
import com.adbest.smsmarketingfront.util.EncryptTools;
import com.adbest.smsmarketingfront.util.twilio.param.PreSendMsg;
import com.adbest.smsmarketingfront.util.twilio.TwilioUtil;
import com.twilio.rest.api.v2010.account.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsMarketingFrontApplicationTests {
    
    @Autowired
    TwilioUtil twilioUtil;
    @Autowired
    ResourceBundle res;
    @Autowired
    MessageTask messageTask;
    @Autowired
    EncryptTools encryptTools;
    
    @Test
    public void contextLoads() {
//        QContacts qContacts = QContacts.contacts;
//        BooleanBuilder builder = new BooleanBuilder();
//        builder.and(qContacts.customerId.eq(2L));
        PreSendMsg preSendMsg = new PreSendMsg();
        MessageRecord record = new MessageRecord();
        record.setContactsNumber("+12144051403");
        record.setCustomerNumber("+16782758458");
        record.setContent("From twilio -- test message");
        preSendMsg.setRecord(record);
        Message message = twilioUtil.sendMessage(preSendMsg);
        System.out.println(message);
    }
    
    @Test
    public void testBundle() throws UnsupportedEncodingException {
        System.out.println(res.getLocale());
        System.out.println(res.getString("lang-test"));
        System.out.println(new String(res.getString("lang-test").getBytes(StandardCharsets.ISO_8859_1), "GBK"));
    }
    
    
    @Test
    public void testGenerateSendMsgThread(){
//        messageTask.distributeSendMsgJob();
        System.out.println("encrypt: "+encryptTools.encrypt("123123")); // ecc44937642cd28e9491f10756e7df39
    }
    
}
