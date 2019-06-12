package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingfront.entity.dto.CustomerDto;
import com.adbest.smsmarketingfront.entity.form.CustomerForm;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public interface CustomerService extends UserDetailsService {

    public Customer save(Customer customer);

    public List<Customer> saveAll(List<Customer> customers);

    public Customer update(Customer customer);

    Customer findFirstByEmailAndPassword(String username, String encrypt);

    CustomerVo register(CustomerForm vo, HttpServletRequest request);

    Customer findFirstByEmail(String email);

    List<Customer> findByEmailIn(List<String> emails);

    void initCustomerData(Customer customer);

    boolean changePassword(String password, String newPassword);

    CustomerVo updateInfo(CustomerForm customerForm);

    boolean getCode(String email, HttpServletRequest request);

    boolean updatePasswordByCode(String code, String password, HttpServletRequest request);

    BufferedImage createVerifyCode(HttpServletRequest request);

    Customer findById(Long customerId);

    void saveImportCustomer(List<Customer> customerList);
}
