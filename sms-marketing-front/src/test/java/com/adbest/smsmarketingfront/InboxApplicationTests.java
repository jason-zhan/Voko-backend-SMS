package com.adbest.smsmarketingfront;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.entity.enums.VokoPayStatus;
import com.adbest.smsmarketingfront.entity.vo.VokoPayVo;
import com.adbest.smsmarketingfront.service.ContactsGroupService;
import com.adbest.smsmarketingfront.service.KeywordService;
import com.adbest.smsmarketingfront.service.MessageRecordService;
import com.adbest.smsmarketingfront.util.ObjectConvertUtils;
import com.adbest.smsmarketingfront.util.VokophonePayUtils;
import com.adbest.smsmarketingfront.util.twilio.TwilioUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

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

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    VokophonePayUtils vokophonePayUtils;

    @Test
    public void test() throws Exception {
//        InboundMsg inboundMsg = new InboundMsg();
//        inboundMsg.setMessageSid(UUID.randomUUID().toString());
//        inboundMsg.setBody("优惠");
//        inboundMsg.setFrom("123456");
//        inboundMsg.setTo("+123456789");
//        messageRecordService.saveInbox(inboundMsg);
//        MessageRecord send = new MessageRecord();
//        send.setCustomerNumber("+12565768219");
//        send.setContent("qq00test..");
//        send.setSms(true);
//        send.setContactsNumber("");
//        send.setInbox(false);
//        send.setDisable(false);
//        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//        send.setSendTime(timestamp);
//        send.setExpectedSendTime(timestamp);
//        send.setStatus(OutboxStatus.SENT.getValue());
//        PreSendMsg preSendMsg = new PreSendMsg(send);
//        twilioUtil.sendMessage(preSendMsg);
//
//        Pageable pageRequest = PageRequest.of(0,100);
//        long time = System.currentTimeMillis()-1000*60*60*24*3;
//        List<?> objects = vkCDRAccountsDao.selectEffectiveData(new Timestamp(time),new Timestamp(time+1000*60*3),pageRequest);
//        System.out.println(objects.size());

//        List<?> objects = vkCDRAccountsDao.selectNeedToSend(Arrays.asList(4673510, 4673509, 4673508, 4673507, 4673506, 4673505, 4673504));
//        System.out.println(objects.size());
    }
}
