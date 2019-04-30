package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import com.adbest.smsmarketingfront.service.CustomerService;
import com.adbest.smsmarketingfront.util.CommonMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class CustomerServiceImpl implements  CustomerService {

    @Autowired
    private CustomerDao customerDao;

    @Override
    public Customer save(Customer customer) {
        return customerDao.save(customer);
    }

    @Override
    public List<Customer> saveAll(List<Customer> customers) {
        return customerDao.saveAll(customers);
    }

    @Override
    public Customer update(Customer customer) {
        return customerDao.save(customer);
    }

    public Customer findFirstByEmailAndPassword(String username, String encrypt) {
        return customerDao.findFirstByEmailAndPassword(username,encrypt);
    }

    @Override
    public boolean register(CustomerVo createSysUser) {
        Assert.notNull(createSysUser, CommonMessage.PARAM_IS_NULL);
        Assert.hasText(createSysUser.getEmail(), "邮箱" + CommonMessage.CAN_NOT_EMPTY);
        Assert.hasText(createSysUser.getPassword(), "密码" + CommonMessage.CAN_NOT_EMPTY);
        // 用户名、密码正则校验
        Assert.isTrue(Customer.checkEmail(createSysUser.getEmail()), "邮箱格式有误");
        Assert.isTrue(Customer.checkPassword(createSysUser.getPassword()), "密码必须为5-25位的字母或数字");
        Customer repeat = customerDao.findFirstByUsername(createSysUser.getEmail());
        Assert.isNull(repeat, "该邮箱已存在");
        Customer customer = new Customer();

        customerDao.save(customer);
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Customer customer = customerDao.findByEmail(s);
        if (customer == null) {
            throw new UsernameNotFoundException("邮箱错误");
        }
        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return new ArrayList<GrantedAuthority>();
            }

            @Override
            public String getPassword() {
                return customer.getPassword();
            }

            @Override
            public String getUsername() {
                return customer.getEmail();
            }

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                return true;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isEnabled() {
                return !customer.getDisable();
            }
        };
    }
}