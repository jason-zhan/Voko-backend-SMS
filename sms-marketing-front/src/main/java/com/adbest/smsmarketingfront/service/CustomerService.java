package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingfront.entity.dto.CustomerDto;
import com.adbest.smsmarketingfront.entity.form.CustomerForm;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;
import java.util.List;

public interface CustomerService extends UserDetailsService {

    public Customer save(Customer customer);

    public List<Customer> saveAll(List<Customer> customers);

    public Customer update(Customer customer);

    Customer findFirstByEmailAndPassword(String username, String encrypt);

    boolean register(CustomerForm vo);

    Customer findFirstByEmail(String email);

    List<Customer> findByEmailIn(List<String> emails);

    void initCustomerData(Customer customer);
}
