package com.adbest.smsmarketingfront.task;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.entity.dto.CustomerDto;
import com.adbest.smsmarketingfront.entity.dto.VkCDRAccountsDto;
import com.adbest.smsmarketingfront.entity.enums.ContactsSource;
import com.adbest.smsmarketingfront.entity.enums.CustomerSource;
import com.adbest.smsmarketingfront.entity.enums.VkCDRCustomersSendStatus;
import com.adbest.smsmarketingfront.service.*;
import com.adbest.smsmarketingfront.util.EncryptTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.adbest.smsmarketingentity.QContactsTemp.contactsTemp;

@Component
@EnableAsync
@Slf4j
public class DbImportCustomerTask {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private VkCustomersService vkCustomersService;

    @Autowired
    private ContactsService contactsService;

    @Autowired
    private CustomerSettingsService customerSettingsService;

    @Autowired
    private MobileNumberService mobileNumberService;

    @Autowired
    private MessageRecordService messageRecordService;

    @Autowired
    private EncryptTools encryptTools;

    @Autowired
    private VkCDRAccountsService vkCDRAccountsService;

    @Scheduled(cron = "30 39 15 * * ?")
    //@Scheduled(cron = "15 0/10 * * * ?")
    public void importCustomerTask(){
        int size = 1000;
        int page = 0;
        do {
            PageRequest pageRequest = PageRequest.of(page,size);
            List<VkCustomers> list = vkCustomersService.findByInLeadinIsNull(pageRequest);
            page++;
            if (list.size()<=0){break;}
            Map<String, VkCustomers> map = list.stream().collect(Collectors.toMap(VkCustomers::getLogin, vkCustomers -> vkCustomers, (vc, newVc) -> vc));
            List<Customer> customers = customerService.findByCustomerLoginIn(new ArrayList<>(map.keySet()));
            if (customers.size()>0){
                for (Customer c : customers) {
                    map.remove(c.getCustomerLogin());
                }
                vkCustomersService.updateInLeadinByLoginIn(false, customers.stream().map(customer -> customer.getCustomerLogin()).collect(Collectors.toList()));
            }
            intoCustomer(map);
            if (list.size()<size){break;}
        }while (true);
    }

    public void intoCustomer(Map<String, VkCustomers> map){
        if (map.size()<=0){return;}
        List<Customer> customerList = new ArrayList<>();
        Customer customer = null;
        for (String login : map.keySet()) {
            VkCustomers vkCustomers = map.get(login);
            customer = new Customer();
            customer.setPassword(UUID.randomUUID().toString());
            customer.setDisable(false);
            customer.setEmail(vkCustomers.getEmail());
            customer.setFirstName(vkCustomers.getFirstname());
            customer.setLastName(vkCustomers.getLastname());
            customer.setSource(CustomerSource.API_Added.getValue());
            customer.setCustomerLogin(vkCustomers.getLogin());
            customer.setCredit(BigDecimal.valueOf(0));
            customer.setCustomerLogin(vkCustomers.getLogin());
            customer.setVkCustomersId(vkCustomers.getI_customer());
            customer.setPassword(encryptTools.encrypt(vkCustomers.getPassword()));
            customerList.add(customer);
        }
        customerService.saveImportCustomer(customerList);
    }

    @Scheduled(cron = "0/30 * * * * ?")
    public void importContactsTask(){
//        int size = 500;
//        int page = 0;
//        boolean is = true;
//        do {
//            PageRequest pageRequest = PageRequest.of(page,size);
//            List<?> list = vkCDRCustomersService.selectImportablePhone(pageRequest);
//            page++;
//            importContacts(list);
//            if (list.size()<size){is = false;break;}
//        }while (is);
//        vkCDRCustomersService.updateRepeatInLeadin();
//        sendSms();


        int size = 500;
        int page = 0;
        boolean is = true;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis() - 3 * 60 * 1000);
        do {
            PageRequest pageRequest = PageRequest.of(page,size);
            List<?> list = vkCDRAccountsService.selectEffectiveData(timestamp,pageRequest);
            page++;
            if (list.size()==0){break;}else if (list.size()<size){is = false;}
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            vkCDRAccountsService.saveContacts(list);
            vkCDRAccountsService.sendSms(list);
            list.clear();
        }while (is);
    }

    /*public void sendSms(){
        int size = 500;
        int page = 0;
        boolean is = true;
        Map<Long, CustomerSettings> settingsMap = null;
        List<MessageRecord> messageRecords = new ArrayList<>();
        List<?> list = null;
        List<Long> customerId = null;
        List<CustomerSettings> customerSettingsList = null;
        List<Integer> notSendIds = new ArrayList<>();
        List<Integer> sendIds = new ArrayList<>();
        List<MobileNumber> numbers = null;
        Map<Long, MobileNumber> numberMap = null;
        do {
            PageRequest pageRequest = PageRequest.of(page,size);
            list = vkCDRCustomersService.selectSendPhone(new Timestamp(System.currentTimeMillis()-3*60*1000), pageRequest);
             if (list.size()==0){is = false;break;}
            page++;
            customerId = list.stream().map(s -> Long.valueOf(((Object[]) s)[3].toString())).distinct().collect(Collectors.toList());
            customerSettingsList = customerSettingsService.findByCustomerIdInAndCallReminder(customerId, true);
            if (customerSettingsList.size()>0){
                settingsMap = customerSettingsList.stream().collect(Collectors.toMap(CustomerSettings::getCustomerId, customerSettings -> customerSettings));
                numbers = mobileNumberService.findByCustomerIdInAndDisable(new ArrayList<>(settingsMap.keySet()), false);
                numberMap = numbers.stream().collect(Collectors.toMap(MobileNumber::getCustomerId, mobileNumber -> mobileNumber, (mobileNumber1, mobileNumber2) -> mobileNumber1));
                for (Object obj : list) {
                    Object[] objects = (Object[]) obj;
                    CustomerSettings customerSettings = settingsMap.get(Long.valueOf(objects[3].toString()));
                    if (customerSettings==null){
                        notSendIds.add(Integer.valueOf(objects[0].toString()));
                        continue;
                    }else {
                        sendIds.add(Integer.valueOf(objects[0].toString()));
                    }
                    MobileNumber mobileNumber = numberMap.get(Long.valueOf(objects[3].toString()));
                    if(mobileNumber==null){
                        notSendIds.add(Integer.valueOf(objects[0].toString()));
                        sendIds.remove(Integer.valueOf(objects[0].toString()));
                        continue;
                    }
                    String content = customerSettings.getContent();
                    MessageRecord send = new MessageRecord();
                    send.setCustomerId(Long.valueOf(objects[3].toString()));
                    send.setCustomerNumber(mobileNumber.getNumber());
                    content = content.replaceAll(MsgTemplateVariable.CON_FIRSTNAME.getTitle(), StringUtils.isEmpty(objects[4]) ? "" : objects[4].toString())
                            .replaceAll(MsgTemplateVariable.CON_LASTNAME.getTitle(), StringUtils.isEmpty(objects[5]) ? "" : objects[5].toString())
                            .replaceAll(MsgTemplateVariable.CUS_FIRSTNAME.getTitle(), StringUtils.isEmpty(objects[6]) ? "" : objects[6].toString())
                            .replaceAll(MsgTemplateVariable.CUS_LASTNAME.getTitle(), StringUtils.isEmpty(objects[7]) ? "" : objects[7].toString());
                    send.setContent(content);
                    send.setSms(true);
                    send.setContactsId(Long.valueOf(objects[2].toString()));
                    send.setContactsNumber(objects[1].toString());
                    send.setInbox(false);
                    send.setDisable(false);
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    send.setSendTime(timestamp);
//                    send.setExpectedSendTime(timestamp);
                    send.setStatus(OutboxStatus.SENT.getValue());
                    messageRecords.add(send);
                }

                messageRecordService.sendCallReminder(messageRecords);
                messageRecords.clear();
                if (notSendIds.size()>0){vkCDRCustomersService.updateSendStatus(notSendIds, VkCDRCustomersSendStatus.UNWANTED_SENT.getValue());}
                notSendIds.clear();
                if(sendIds.size()>0){vkCDRCustomersService.updateSendStatus(sendIds, VkCDRCustomersSendStatus.ALREADY_SENT.getValue());}
                sendIds.clear();
            }else {
                notSendIds = list.stream().map(s -> Integer.valueOf(((Object[])s)[0].toString())).collect(Collectors.toList());
                vkCDRCustomersService.updateSendStatus(notSendIds, VkCDRCustomersSendStatus.UNWANTED_SENT.getValue());
            }
            if (list.size()<size){is = false;break;}
        }while (is);
    }

    @Transactional
    public void importContacts(List<?> list){
        List<Contacts> contactsList = null;
        Contacts contacts = null;
        if (list.size()>0){
            contactsList = new ArrayList<>();
            for (Object obj : list) {
                Object[] objects = (Object[]) obj;
                contacts = new Contacts();
                String phone = objects[1]+"";
                phone = phone.startsWith("+1")?phone.substring(2,phone.length()):phone;
                contacts.setPhone(phone);
                contacts.setSource(ContactsSource.API_Added.getValue());
                contacts.setCustomerId((Long) objects[4]);
                contacts.setInLock(false);
                contacts.setIsDelete(false);
                contactsList.add(contacts);
            }
            contactsService.saveAll(contactsList);
            List<Integer> ids = list.stream().map(s -> Integer.valueOf(((Object[]) s)[0].toString())).collect(Collectors.toList());
            vkCDRCustomersService.updateInLeadin(true, ids);
        }
    }*/

}
