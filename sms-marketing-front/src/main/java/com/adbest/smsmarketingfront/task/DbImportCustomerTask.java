package com.adbest.smsmarketingfront.task;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingentity.VkCDRCustomers;
import com.adbest.smsmarketingentity.VkCustomers;
import com.adbest.smsmarketingfront.entity.dto.CustomerDto;
import com.adbest.smsmarketingfront.entity.enums.ContactsSource;
import com.adbest.smsmarketingfront.service.ContactsService;
import com.adbest.smsmarketingfront.service.CustomerService;
import com.adbest.smsmarketingfront.service.VkCDRCustomersService;
import com.adbest.smsmarketingfront.service.VkCustomersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
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

    @Scheduled(cron = "30 0 0/1 * * ?")
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
        Customer customer = null;
        for (String email : map.keySet()) {
            VkCustomers vkCustomers = map.get(email);
            customer = new Customer();
            customer.setPassword(UUID.randomUUID().toString());
            customer.setDisable(false);
            customer.setEmail(vkCustomers.getEmail());
            customer.setCustomerName(vkCustomers.getName());
            customer.setFirstName(vkCustomers.getFirstname());
            customer.setLastName(vkCustomers.getLastname());
            customerList.add(customer);
        }
        customerService.saveAll(customerList);
        vkCustomersService.updateInLeadinByEmailIn(true, customerList.stream().map(c -> c.getEmail()).collect(Collectors.toList()));
        for (Customer c : customerList) {
            customerService.initCustomerData(c);
        }
    }

    @Scheduled(cron = "0/30 * * * * ?")
    public void importContactsTask(){
        int size = 500;
        int page = 0;
        boolean is = true;
        do {
            PageRequest pageRequest = PageRequest.of(page,size);
            List<?> list = vkCDRCustomersService.selectImportablePhone(pageRequest);
            page++;
            importContacts(list);
            if (list.size()<size){is = false;return;}
        }while (is);
        vkCDRCustomersService.updateRepeatInLeadin();
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
                contacts.setPhone(objects[1]+"");
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
