package com.adbest.smsmarketingfront.task;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.entity.enums.CustomerSource;
import com.adbest.smsmarketingfront.service.*;
import com.adbest.smsmarketingfront.util.EncryptTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Component
@EnableAsync
@Slf4j
public class DbImportCustomerTask {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private VkCustomersService vkCustomersService;

    @Autowired
    private EncryptTools encryptTools;

    @Autowired
    private VkCDRAccountsService vkCDRAccountsService;

    @Autowired
    private Environment environment;
    @Scheduled(cron = "15 0/10 * * * ?")
    public void importCustomerTask(){
        String taskSwitch = environment.getProperty("taskSwitch");
        if (taskSwitch==null||!Boolean.valueOf(taskSwitch)){return;}

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
            customer.setAvailableCredit(BigDecimal.valueOf(0));
            customer.setMaxCredit(BigDecimal.valueOf(0));
            customer.setCustomerLogin(vkCustomers.getLogin());
            customer.setVkCustomersId(vkCustomers.getI_customer());
            customer.setPassword(encryptTools.encrypt(encryptTools.vkPasswordDecrypt(vkCustomers.getPassword())));
            customerList.add(customer);
        }
        customerService.saveImportCustomer(customerList);
    }

    @Scheduled(cron = "0/30 * * * * ?")
    public void importContactsTask(){
        String taskSwitch = environment.getProperty("taskSwitch");
        if (taskSwitch==null||!Boolean.valueOf(taskSwitch)){return;}
        int size = 500;
        int page = 0;
        boolean is = true;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis() - 3 * 60 * 1000);
        do {
            PageRequest pageRequest = PageRequest.of(page,size);
            List<?> list = vkCDRAccountsService.selectEffectiveData(timestamp,pageRequest);
            page++;
            if (list.size()==0){break;}else if (list.size()<size){is = false;}
            vkCDRAccountsService.saveContacts(list);
            vkCDRAccountsService.sendSms(list);
            list.clear();
        }while (is);
    }

}
