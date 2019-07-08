package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingentity.ContactsLinkGroup;
import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingfront.dao.ContactsDao;
import com.adbest.smsmarketingfront.dao.ContactsLinkGroupDao;
import com.adbest.smsmarketingfront.service.param.CreateMessagePlan;
import com.adbest.smsmarketingfront.service.param.UpdateMessagePlan;
import com.adbest.smsmarketingfront.util.EasyTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MessagePlanServiceTest {
    
    @Autowired
    MessagePlanService messagePlanService;
    @Autowired
    ContactsDao contactsDao;
    @Autowired
    ContactsLinkGroupDao contactsLinkGroupDao;
    
    @Test
    public void create() {
        CreateMessagePlan create = new CreateMessagePlan();
        create.setFromNumList(Arrays.asList("0100000", "1111111"));
//        create.setGroupList(Arrays.asList(1L));
        create.setToNumberList(Arrays.asList("0000001", "0000002", "0000003", "0000004", "0000005"));
        create.setRemark("test create plan");
        create.setTitle("2019-7-8 11:46:33 test");
        create.setText("Hello! There is twilio agent service center, how do you do?");
        create.setExecTime(EasyTime.init().addDays(1).stamp());
        messagePlanService.create(create);
    }
    
    @Test
    public void createInstant() {
        CreateMessagePlan create = new CreateMessagePlan();
        create.setFromNumList(Arrays.asList("0100000", "1111111"));
//        create.setGroupList(Arrays.asList(1L));
        create.setToNumberList(Arrays.asList("0000001", "0000002", "0000003", "0000004", "0000005"));
        create.setRemark("test create send-immediately plan");
        create.setTitle("2019-7-8 16:32:33 test");
        create.setText("Hello! There is twilio agent service center, how do you do?");
//        create.setExecTime(EasyTime.init().addDays(1).stamp());
        messagePlanService.createInstant(create);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void update() {
        UpdateMessagePlan update = new UpdateMessagePlan();
        update.setId(7L);
        update.setFromNumList(Arrays.asList("6666666", "0100000"));
        update.setText("Hello world! Hello XiaMi!");
        update.setTitle("update a plan");
        update.setToNumberList(Arrays.asList("0000004","0000006","0000008","6666666"));
        update.setExecTime(EasyTime.init().addDays(2).stamp());
        update.setRemark("update test");
        messagePlanService.update(update);
    }
    
    @Test
    public void checkCrossBeforeCancel() {
    
        boolean allow = messagePlanService.checkCrossBeforeCancel(9L);
        System.out.println(allow);
    }
    
    @Test
    public void cancel() {
        messagePlanService.cancel(7L);
    }
    
    @Test
    public void restart() {
        messagePlanService.restart(7L);
    }
    
    @Test
    public void delete() {
    }
    
    @Test
    public void findById() {
    }
    
    @Test
    public void findByConditions() {
    }
    
    @Test
    public void statusMap() {
    }
    
    private void createBatchContacts() {
        int phoneFrom = 0;
        int travel = 100;
        Long customerId = 1L;
        Long groupId = 1L;
        while (travel > 0) {
            List<Contacts> contactsList = new ArrayList<>();
            List<ContactsLinkGroup> linkList = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                // 生成联系人
                Contacts contacts = new Contacts();
                contacts.setId((long) phoneFrom);
                contacts.setCustomerId(customerId);
                contacts.setPhone(getFormatPhone(phoneFrom));
                contacts.setFirstName("test_" + phoneFrom);
                contacts.setLastName("group_" + groupId);
                contacts.setInLock(false);
                contacts.setIsDelete(false);
                contacts.setSource(1);
                // 生成联系人-群组链接表
                ContactsLinkGroup link = new ContactsLinkGroup(contacts.getId(), groupId);
                contactsList.add(contacts);
                linkList.add(link);
                phoneFrom++;
            }
            contactsDao.saveAll(contactsList);
            contactsLinkGroupDao.saveAll(linkList);
            travel--;
        }
        
        
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
}