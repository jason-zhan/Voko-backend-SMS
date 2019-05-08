package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.entity.form.CustomerForm;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.CustomerService;
import com.adbest.smsmarketingfront.service.UsAreaService;
import com.adbest.smsmarketingfront.util.CommonMessage;
import com.adbest.smsmarketingfront.util.EncryptTools;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

@Service
public class CustomerServiceImpl implements  CustomerService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private EncryptTools encryptTools;

    @Autowired
    private UsAreaService usAreaService;

    @Autowired
    private ReturnMsgUtil returnMsgUtil;

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
    @Transactional
    public boolean register(CustomerForm createSysUser) {
        ServiceException.notNull(createSysUser, returnMsgUtil.msg("PARAM_IS_NULL"));
        ServiceException.hasText(createSysUser.getEmail(), returnMsgUtil.msg("EMAIL_NOT_EMPTY"));
        ServiceException.hasText(createSysUser.getPassword(), returnMsgUtil.msg("PASSWORD_NOT_EMPTY"));
        // 用户名、密码正则校验
        ServiceException.isTrue(Customer.checkEmail(createSysUser.getEmail()), returnMsgUtil.msg("EMAIL_INCORRECT_FORMAT"));
        ServiceException.isTrue(Customer.checkPassword(createSysUser.getPassword()), returnMsgUtil.msg("PASSWORD_INCORRECT_FORMAT"));
        Customer repeat = customerDao.findFirstByEmail(createSysUser.getEmail());
        ServiceException.isNull(repeat, returnMsgUtil.msg("EMAIL_EXISTS"));
        Customer customer = new Customer();
        customer.setPassword(encryptTools.encrypt(createSysUser.getPassword()));
        customer.setDisable(false);
        customer.setEmail(createSysUser.getEmail());
        if (createSysUser.getCity()!=null)customer.setCity(usAreaService.findById(Long.valueOf(createSysUser.getCity())));
        if (createSysUser.getState()!=null)customer.setState(usAreaService.findById(Long.valueOf(createSysUser.getState())));
        customer.setCustomerName(createSysUser.getCustomerName());
        customer.setFirstName(createSysUser.getFirstName());
        customer.setLastName(createSysUser.getLastName());
        customer.setIndustry(createSysUser.getIndustry());
        customer.setOrganization(createSysUser.getOrganization());
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
