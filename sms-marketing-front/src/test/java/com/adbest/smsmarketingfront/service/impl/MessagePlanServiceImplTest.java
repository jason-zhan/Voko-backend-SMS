package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingentity.ContactsLinkGroup;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.adbest.smsmarketingfront.dao.ContactsDao;
import com.adbest.smsmarketingfront.dao.ContactsLinkGroupDao;
import com.adbest.smsmarketingfront.dao.MessageRecordDao;
import com.adbest.smsmarketingfront.service.MessagePlanService;
import com.adbest.smsmarketingfront.service.SmsBillComponent;
import com.adbest.smsmarketingfront.service.param.CreateMessagePlan;
import com.adbest.smsmarketingfront.util.TimeTools;
import com.adbest.smsmarketingfront.util.twilio.MessageTools;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@RunWith(SpringRunner.class)
@SpringBootTest
public class MessagePlanServiceImplTest {
    
    @Autowired
    private MessagePlanService messagePlanService;
    @Autowired
    private ContactsDao contactsDao;
    @Autowired
    private ContactsLinkGroupDao contactsLinkGroupDao;
    @Autowired
    private MessageRecordDao messageRecordDao;

    @Test
    public void testStr() {
//        String chStr = "\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341";
        String chStr = "我是中国人我是中国人";
        String enStr = "12345 abcde \n \r\n \t";
        System.out.println("== ==");
        System.out.println("ch len: " + chStr.length());
        System.out.println(chStr);
        System.out.println("ch isGsm7: " + isGsm7(chStr));
        System.out.println("en isGsm7: " + isGsm7(enStr));
        System.out.println("== ==");
//        String str = "(To the Oak Tree)" +
//                "I must be a ceiba tree beside you " +
//                "Be the image of a tree standing together with you " +
//                "Our roots, entwined underground " +
//                "Our leaves, touching in the clouds " +
//                "With each gust of wind " +
//                "We greet each other " +
//                "But nobody " +
//                "Can understand our words ";
//        int segments = MessageTools.calcMsgSegments(str);
//        System.out.println("segments: "+segments);
    }
    
    @Test
    public void testRemainder() {
        System.out.println(5 % 5);
    }
    
    
    @Test
    public void testCreatePlan() {
        CreateMessagePlan create = new CreateMessagePlan();
        create.setTitle("测试任务2");
        create.setText("(To the Oak Tree)" +
                "I must be a ceiba tree beside you " +
                "Be the image of a tree standing together with you " +
                "Our roots, entwined underground " +
                "Our leaves, touching in the clouds " +
                "With each gust of wind " +
                "We greet each other " +
                "But nobody " +
                "Can understand our words ");
        create.setExecTime(new Timestamp(1558324820000L));
        create.setFromList(Arrays.asList(1L, 2L, 3L));
//        create.setToList(Arrays.asList(3L, 4L, 5L, 6L));
        create.setGroupList(Arrays.asList(1L));
        messagePlanService.create(create);
    }
    
    @Test
    public void batchCreateContacts() {
        int addAmount = 12100;
        Long groupId = 1L;
        Long idStart = 1L;
        List<Contacts> contactsList = new ArrayList<>();
        List<ContactsLinkGroup> linkList = new ArrayList<>();
        for (int i = 0; i < addAmount; i++) {
            Contacts newly = new Contacts();
            newly.setId(idStart + i);
            newly.setCustomerId(1L);
            newly.setPhone(getFormatPhone(i));
            newly.setFirstName(i + "");
            newly.setLastName("group" + groupId);
            newly.setInLock(false);
            newly.setIsDelete(false);
            newly.setSource(2);
            contactsList.add(newly);
            linkList.add(new ContactsLinkGroup(newly.getId(), groupId));
            if (contactsList.size() > 999) {
                System.out.println("now rate " + i + "/" + addAmount);
                batchSaveContacts(contactsList, linkList);
                contactsList.clear();
                linkList.clear();
                continue;
            }
        }
        if (contactsList.size() > 0) {
            batchSaveContacts(contactsList, linkList);
        }
    }
    
    @Test
    public void testUpdateMsgStatus() {
        Page<MessageRecord> msgPage = messageRecordDao.findByPlanIdAndStatusAndDisableIsFalse(2L, 2, PageRequest.of(0, 1000));
        for (MessageRecord msg : msgPage.getContent()) {
            msg.setSid(UUID.randomUUID().toString());
            messageRecordDao.updateStatusAfterSendMessage(msg.getId(),msg.getSid(),OutboxStatus.SENT.getValue());
        }
    }
    
    
    private static boolean isGsm7(String str) {
        int strLen = str.length();
        int byteLen = str.getBytes().length;
        if (strLen == byteLen) {
            return true;
        }
        return false;
    }
    
    private String getFormatPhone(int i) {
        StringBuilder sb = new StringBuilder();
        sb.append(i);
        int bits = 7;
        for (int j = sb.length(); j < bits; j++) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }
    
    @Transactional
    public void batchSaveContacts(List<Contacts> contactsList, List<ContactsLinkGroup> linkList) {
        contactsDao.saveAll(contactsList);
        contactsLinkGroupDao.saveAll(linkList);
    }
}