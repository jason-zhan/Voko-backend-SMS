package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.entity.dto.CustomerDto;
import com.adbest.smsmarketingfront.entity.form.CustomerForm;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import com.adbest.smsmarketingfront.entity.vo.UserDetailsVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.*;
import com.adbest.smsmarketingfront.util.CommonMessage;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.EncryptTools;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import com.adbest.smsmarketingfront.util.twilio.TwilioUtil;
import com.twilio.base.ResourceSet;
import com.twilio.rest.api.v2010.account.IncomingPhoneNumber;
import com.twilio.rest.api.v2010.account.availablephonenumbercountry.Local;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
public class CustomerServiceImpl implements  CustomerService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private EncryptTools encryptTools;

    @Autowired
    private UsAreaService usAreaService;

    @Autowired
    private ReturnMsgUtil returnMsgUtil;

    @Autowired
    private MarketSettingService marketSettingService;

    @Autowired
    private TwilioUtil twilioUtil;

    @Autowired
    private MobileNumberService mobileNumberService;

    @Autowired
    private KeywordService keywordService;

    @Autowired
    private MmsBillComponent mmsBillComponent;

    @Autowired
    private SmsBillComponentImpl smsBillComponent;

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
//        if (createSysUser.getCity()!=null)customer.setCity(usAreaService.findById(Long.valueOf(createSysUser.getCity())));
//        if (createSysUser.getState()!=null)customer.setState(usAreaService.findById(Long.valueOf(createSysUser.getState())));
        customer.setCustomerName(createSysUser.getCustomerName());
        customer.setFirstName(createSysUser.getFirstName());
        customer.setLastName(createSysUser.getLastName());
        customer.setIndustry(createSysUser.getIndustry());
        customer.setOrganization(createSysUser.getOrganization());
        customerDao.save(customer);
        new Thread(){
            public void run() {
                initCustomerData(customer);
            }
        }.start();
        return true;
    }

    @Override
    public Customer findFirstByEmail(String email) {
        return customerDao.findFirstByEmail(email);
    }

    @Override
    public List<Customer> findByEmailIn(List<String> emails) {
        return customerDao.findByEmailIn(emails);
    }

    @Override
    public void initCustomerData(Customer customer){
        //初始化手机号码
        initPhone(customer,1);
        List<MarketSetting> marketSettings = marketSettingService.findAll();
        if (marketSettings.size()<=0)return;
        MarketSetting marketSetting = marketSettings.get(0);
        //初始化关键字
        List<Keyword> keywords = new ArrayList<>();
        Keyword keyword = null;
        if (marketSetting.getKeywordTotal()>0){
            for (int i = 0;i<marketSetting.getKeywordTotal();i++){
                keyword = new Keyword(customer.getId(), UUID.randomUUID().toString().replaceAll("-",""));
                keywords.add(keyword);
            }
            keywordService.saveAll(keywords);
        }
        String infoDescribe ="experience gift";
        //初始化短信条数
        if (marketSetting.getSmsTotal()>0){
            SmsBill smsBill = new SmsBill(customer.getId(),infoDescribe,marketSetting.getSmsTotal());
            smsBillComponent.save(smsBill);
        }
        //初始化彩信条数
        if (marketSetting.getMmsTotal()>0){
            MmsBill mmsBill = new MmsBill(customer.getId(),infoDescribe,marketSetting.getSmsTotal());
            mmsBillComponent.save(mmsBill);
        }
    }

    @Override
    @Transactional
    public boolean changePassword(String password, String newPassword) {
        Long customerId = Current.get().getId();
        ServiceException.hasText(password, returnMsgUtil.msg("PASSWORD_NOT_EMPTY"));
        ServiceException.hasText(newPassword, returnMsgUtil.msg("PASSWORD_NOT_EMPTY"));
        ServiceException.isTrue(Customer.checkPassword(newPassword), returnMsgUtil.msg("PASSWORD_INCORRECT_FORMAT"));
        Customer customer = customerDao.findById(customerId).get();
        ServiceException.isTrue(customer.getPassword().equals(encryptTools.encrypt(password)), returnMsgUtil.msg("PASSWORD_INCORRECT"));
        customer.setPassword(encryptTools.encrypt(newPassword));
        customerDao.save(customer);
        return true;
    }

    @Override
    @Transactional
    public boolean updateInfo(CustomerForm customerForm) {
        Long customerId = Current.get().getId();
        Customer customer = customerDao.findById(customerId).get();
        customer.setOrganization(customerForm.getOrganization());
        customer.setIndustry(customerForm.getIndustry());
        customer.setLastName(customerForm.getLastName());
        customer.setFirstName(customerForm.getFirstName());
        customerDao.save(customer);
        return true;
    }

    public void initPhone(Customer customer,int i){
        if (i>3){
            return;
        }
        i++;
        try {
            ResourceSet<Local> resourceSet = twilioUtil.fetchNumbersByAreaCode(null);
            Iterator<Local> iterator = resourceSet.iterator();
            Local next = iterator.next();
            String phone = next.getPhoneNumber().getEndpoint();
            IncomingPhoneNumber incomingPhoneNumber = twilioUtil.purchaseNumber(phone);
            MobileNumber mobileNumber = new MobileNumber();
            mobileNumber.setCustomerId(customer.getId());
            mobileNumber.setDisable(false);
            mobileNumber.setNumber(incomingPhoneNumber.getPhoneNumber().getEndpoint());
            mobileNumberService.save(mobileNumber);
        }catch (Exception e){
            log.error("初始化:{}用户号码出错,{}",customer.getId(),e);
            initPhone(customer,i);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Customer customer = customerDao.findByEmail(s);
        if (customer == null) {
            throw new UsernameNotFoundException("邮箱错误");
        }
        return new UserDetailsVo(customer);
    }
}
