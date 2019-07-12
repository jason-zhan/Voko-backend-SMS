package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingentity.ContactsLinkGroup;
import com.adbest.smsmarketingfront.dao.ContactsDao;
import com.adbest.smsmarketingfront.dao.ContactsLinkGroupDao;
import com.adbest.smsmarketingfront.entity.vo.MessagePlanVo;
import com.adbest.smsmarketingfront.service.param.CreateMessagePlan;
import com.adbest.smsmarketingfront.service.param.GetMessagePlanPage;
import com.adbest.smsmarketingfront.service.param.UpdateMessagePlan;
import com.adbest.smsmarketingfront.util.EasyTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringRunner;

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
//        createBatchContacts();
        long begin = EasyTime.nowMillis();
        CreateMessagePlan create = new CreateMessagePlan();
        create.setFromNumList(Arrays.asList("1001111", "2002222", "4004444"));
//        create.setToNumberList(Arrays.asList("0000001", "0000002", "0000003", "0000004", "0000005"));
        create.setGroupList(Arrays.asList(2L));
        create.setRemark("test multi repair");
        create.setTitle(EasyTime.init().format("yyyy-MM-dd HH:mm:ss") + " test");
        create.setText("Hello! Here is twilio agent service center, is there anything I can do for you? ");
        create.setExecTime(EasyTime.init().addMinutes(5).stamp());
        messagePlanService.create(create);
        long end = EasyTime.nowMillis();
        System.out.println("time-spend=" + (end - begin));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void createInstant() {
        CreateMessagePlan create = new CreateMessagePlan();
        create.setFromNumList(Arrays.asList("1001111", "2222222", "6666666"));
        create.setToNumberList(Arrays.asList("0000001", "0000002", "0000003", "0000004", "0000005"));
        create.setRemark("test create send-immediately plan");
        create.setTitle("2019-7-11 09:50:14 test");
        create.setText("Hello! There is twilio agent service center, how do you do?");
        messagePlanService.createInstant(create);
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void update() {
        UpdateMessagePlan update = new UpdateMessagePlan();
        update.setId(7L);
        update.setFromNumList(Arrays.asList("1001111", "2002222", "2002222"));
        update.setText("Hello world! Hello XiaMi!");
        update.setTitle("update a plan on " + EasyTime.init().format("yyyy-MM-dd HH:mm:ss"));
        update.setGroupList(Arrays.asList(2L));
        update.setToNumberList(Arrays.asList("0000001", "0000002", "0000003", "0000004", "0000005"));
        update.setExecTime(EasyTime.init().addDays(1).stamp());
        update.setRemark("update test");
        messagePlanService.update(update);
    }
    
    @Test
    public void checkCrossBeforeCancel() {
        boolean allow = messagePlanService.checkCrossBeforeCancel(2L);
        System.out.println(allow);
    }
    
    @Test
    public void cancel() {
        messagePlanService.cancel(5L);
    }
    
    @Test
    public void restart() {
        messagePlanService.restart(7L);
    }
    
    @Test
    public void delete() {
        messagePlanService.delete(5L);
    }
    
    @Test
    public void findById() {
        MessagePlanVo planVo = messagePlanService.findById(3L);
        System.out.printf("planVo={}", planVo);
    }
    
    @Test
    public void findByConditions() {
        GetMessagePlanPage getPlanPage = new GetMessagePlanPage();
        getPlanPage.setKeyword("test");
//        getPlanPage.setStatus(3);
        getPlanPage.setStart(EasyTime.init().addHours(-6).stamp());
        getPlanPage.setEnd(EasyTime.now());
        Page<MessagePlanVo> planVoPage = messagePlanService.findByConditions(getPlanPage);
        System.out.println(planVoPage.getTotalElements());
    }
    
    @Test
    public void statusMap() {
    }
    
    private void createBatchContacts() {
        int phoneFrom = 100001;
        int travel = 1;
        Long customerId = 1L;
        Long groupId = 2L;
        while (travel > 0) {
            System.out.println("pos=" + travel);
            List<Contacts> contactsList = new ArrayList<>();
            List<ContactsLinkGroup> linkList = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
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