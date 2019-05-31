package com.adbest.smsmarketingfront.task;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.entity.dto.CustomerDto;
import com.adbest.smsmarketingfront.entity.enums.ContactsSource;
import com.adbest.smsmarketingfront.entity.enums.CustomerSource;
import com.adbest.smsmarketingfront.entity.enums.VkCDRCustomersSendStatus;
import com.adbest.smsmarketingfront.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static com.adbest.smsmarketingentity.QContactsTemp.contactsTemp;

@Component
@EnableAsync
public class DbImportCustomerTask {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private VkCustomersService vkCustomersService;

    @Autowired
    private VkCDRCustomersService vkCDRCustomersService;

    @Autowired
    private ContactsService contactsService;

    @Autowired
    private CustomerSettingsService customerSettingsService;

    @Autowired
    private MobileNumberService mobileNumberService;

    @Autowired
    private MessageRecordService messageRecordService;

    //@Scheduled(cron = "30 0 0/1 * * ?")
    public void importCustomerTask(){
        List<VkCustomers> list = vkCustomersService.findByInLeadinIsNullAndEmailNotNull();
        if (list.size()<=0){return;}
        Map<String, VkCustomers> map = list.stream().collect(Collectors.toMap(VkCustomers::getEmail, vkCustomers -> vkCustomers, (vc, newVc) -> vc));
        List<Customer> customers = customerService.findByEmailIn(new ArrayList<>(map.keySet()));
        if (customers.size()>0){
            for (Customer c : customers) {
                map.remove(c.getEmail());
            }
            vkCustomersService.updateInLeadinByEmailIn(false, customers.stream().map(customer -> customer.getEmail()).collect(Collectors.toList()));
        }
        intoCustomer(map);
    }

    @Transactional
    public void intoCustomer(Map<String, VkCustomers> map){
        if (map.size()<=0){return;}
        List<Customer> customerList = new ArrayList<>();
        List<CustomerSettings> customerSettingsList = new ArrayList<>();
        Customer customer = null;
        CustomerSettings customerSettings = null;
        for (String email : map.keySet()) {
            VkCustomers vkCustomers = map.get(email);
            customer = new Customer();
            customer.setPassword(UUID.randomUUID().toString());
            customer.setDisable(false);
            customer.setEmail(vkCustomers.getEmail());
            customer.setFirstName(vkCustomers.getFirstname());
            customer.setLastName(vkCustomers.getLastname());
            customer.setSource(CustomerSource.API_Added.getValue());
            customerList.add(customer);
        }
        customerService.saveAll(customerList);
        for (Customer c:customerList) {
            customerSettings = new CustomerSettings(false, customer.getId(), false);
            customerSettingsList.add(customerSettings);
        }
        customerSettingsService.saveAll(customerSettingsList);
        vkCustomersService.updateInLeadinByEmailIn(true, customerList.stream().map(c -> c.getEmail()).collect(Collectors.toList()));
        for (Customer c : customerList) {
            customerService.initCustomerData(c);
        }
    }

    //@Scheduled(cron = "0/30 * * * * ?")
    public void importContactsTask(){
        int size = 500;
        int page = 0;
        boolean is = true;
        do {
            PageRequest pageRequest = PageRequest.of(page,size);
            List<?> list = vkCDRCustomersService.selectImportablePhone(pageRequest);
            page++;
            importContacts(list);
            if (list.size()<size){is = false;break;}
        }while (is);
        vkCDRCustomersService.updateRepeatInLeadin();

    }

    public void sendSms(){
        int size = 500;
        int page = 0;
        boolean is = true;
        Map<Long, CustomerSettings> settingsMap = null;
        List<MessageRecord> messageRecords = null;
        do {
            PageRequest pageRequest = PageRequest.of(page,size);
            List<?> list = vkCDRCustomersService.selectSendPhone(pageRequest);
            if (list.size()==0){is = false;break;}
            page++;
            List<Long> customerId = list.stream().map(s -> Long.valueOf(((Object[]) s)[3].toString())).distinct().collect(Collectors.toList());
            List<CustomerSettings> customerSettingsList = customerSettingsService.findByCustomerIdInAndCallReminder(customerId, true);
            List<Long> notSendIds = new ArrayList<>();
            List<Long> sendIds = new ArrayList<>();
            if (customerSettingsList.size()>0){
                settingsMap = customerSettingsList.stream().collect(Collectors.toMap(CustomerSettings::getCustomerId, customerSettings -> customerSettings));
                List<MobileNumber> numbers = mobileNumberService.findByCustomerIdInAndDisable(new ArrayList<>(settingsMap.keySet()), false);
                Map<Long, MobileNumber> numberMap = numbers.stream().collect(Collectors.toMap(MobileNumber::getCustomerId, mobileNumber -> mobileNumber, (mobileNumber1, mobileNumber2) -> mobileNumber1));
                for (Object obj : list) {
                    Object[] objects = (Object[]) obj;
                    CustomerSettings customerSettings = settingsMap.get(Long.valueOf(objects[3].toString()));
                    if (customerSettings==null){
                        notSendIds.add(Long.valueOf(objects[0].toString()));
                        continue;
                    }else {
                        sendIds.add(Long.valueOf(objects[0].toString()));
                    }
                    String content = customerSettings.getContent();
                    MessageRecord send = new MessageRecord();
                    send.setCustomerId(Long.valueOf(objects[3].toString()));
                    send.setCustomerNumber(numberMap.get(Long.valueOf(objects[3].toString())).getNumber());
                    content = content.replaceAll(MsgTemplateVariable.CON_FIRSTNAME.getTitle(), StringUtils.isEmpty(objects[4].toString()) ? "" : objects[4].toString())
                            .replaceAll(MsgTemplateVariable.CON_LASTNAME.getTitle(), StringUtils.isEmpty(objects[5].toString()) ? "" : objects[5].toString());
                    send.setContent(content);
                    send.setSms(true);
                    send.setContactsId(Long.valueOf(objects[2].toString()));
                    send.setContactsNumber(objects[1].toString());
                    send.setInbox(false);
                    send.setDisable(false);
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    send.setSendTime(timestamp);
                    send.setExpectedSendTime(timestamp);
                    send.setStatus(OutboxStatus.SENT.getValue());
                    messageRecords.add(send);
                }

                messageRecordService.sendCallReminder(messageRecords);
                vkCDRCustomersService.updateSendStatus(notSendIds, VkCDRCustomersSendStatus.UNWANTED_SENT.getValue());
                vkCDRCustomersService.updateSendStatus(sendIds, VkCDRCustomersSendStatus.ALREADY_SENT.getValue());
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
    }
}
