package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingentity.ContactsLinkGroup;
import com.adbest.smsmarketingfront.dao.ContactsDao;
import com.adbest.smsmarketingfront.dao.ContactsLinkGroupDao;
import com.adbest.smsmarketingfront.service.MessagePlanService;
import com.adbest.smsmarketingfront.service.param.CreateMessagePlan;
import com.adbest.smsmarketingfront.util.TimeTools;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
public class MessagePlanServiceImplTest {
    
    @Autowired
    MessagePlanService messagePlanService;
    @Autowired
    ContactsDao contactsDao;
    @Autowired
    ContactsLinkGroupDao contactsLinkGroupDao;
    
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
    }
    
    @Test
    public void testRemainder() {
        System.out.println(5 % 5);
    }
    
    
    @Test
    public void testCreatePlan() {
        CreateMessagePlan create = new CreateMessagePlan();
        create.setTitle("测试任务");
        create.setText("(To the Oak Tree)" +
                "I must be a ceiba tree beside you " +
                "Be the image of a tree standing together with you " +
                "Our roots, entwined underground " +
                "Our leaves, touching in the clouds " +
                "With each gust of wind " +
                "We greet each other " +
                "But nobody " +
                "Can understand our words ");
        create.setExecTime(TimeTools.addDay(TimeTools.now(), 1));
        create.setFromList(Arrays.asList(1L, 2L, 3L));
        create.setGroupList(Arrays.asList(2L));
        messagePlanService.create(create);
    }
    
    @Test
    public void batchCreateContacts() {
        int addAmount = 10000;
        Long groupId = 2L;
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
            }
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