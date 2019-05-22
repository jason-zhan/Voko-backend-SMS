package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.entity.dto.CustomerDto;
import com.adbest.smsmarketingfront.entity.form.CustomerForm;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import com.adbest.smsmarketingfront.entity.vo.UserDetailsVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.*;
import com.adbest.smsmarketingfront.util.*;
import com.adbest.smsmarketingfront.util.twilio.TwilioUtil;
import com.twilio.base.ResourceSet;
import com.twilio.rest.api.v2010.account.IncomingPhoneNumber;
import com.twilio.rest.api.v2010.account.availablephonenumbercountry.Local;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${spring.mail.username}")
    private String emailname;

    @Autowired
    private AuthenticationManager authenticationManager;

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
    public boolean register(CustomerForm createSysUser, HttpServletRequest request) {
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

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(customer.getEmail(), createSysUser.getPassword());
        Authentication authenticatedUser = authenticationManager
        .authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
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

    @Override
    public boolean getCode(String email, HttpServletRequest request) {
        ServiceException.hasText(email,returnMsgUtil.msg("EMAIL_NOT_EMPTY"));
        Customer customer = customerDao.findFirstByEmail(email);
        ServiceException.notNull(customer,returnMsgUtil.msg("ACCOUNT_NOT_REGISTERED"));
        String number = getNumber(7);
        request.getSession().setAttribute("email", email);
        redisTemplate.opsForValue().set(email, number, 10, TimeUnit.MINUTES);
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("code",number);
        String emailText = createTemplates(dataMap,"emailTemplates");
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            helper.setFrom(emailname);
            helper.setTo(email);
            helper.setSubject("Your Password Reset Request");
            helper.setText(emailText, true);
        } catch (MessagingException e) {
            log.error("发送邮件错误，{}",e);
            return false;
        }
        javaMailSender.send(message);
        return true;
    }

    @Override
    public boolean updatePasswordByCode(String code, String password, HttpServletRequest request) {
        Object email = request.getSession().getAttribute("email");
        ServiceException.notNull(email,returnMsgUtil.msg("VERIFICATION_INFO_EXPIRED"));
        Object c = redisTemplate.opsForValue().get(email.toString());
        ServiceException.notNull(c,returnMsgUtil.msg("VERIFICATION_INFO_EXPIRED"));
        ServiceException.isTrue(c.toString().equals(code),returnMsgUtil.msg("VERIFICATION_CODE_ERROR"));
        redisTemplate.delete(email.toString());
        Customer customer = customerDao.findFirstByEmail(email.toString());
        ServiceException.isTrue(!customer.getPassword().equals(encryptTools.encrypt(password)), returnMsgUtil.msg("PASSWORD_INCORRECT"));
        customer.setPassword(encryptTools.encrypt(password));
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

    /**
     * 根据位数生成验证码
     *
     * @param size 位数
     * @return
     */
    private String getNumber(int size) {
        String retNum = "";
        String codeStr = "1234567890";
        Random r = new Random();
        for (int i = 0; i < size; i++) {
            retNum += codeStr.charAt(r.nextInt(codeStr.length()));
        }
        return retNum;
    }

    /**
     *
     * @param dataMap 渲染数据原
     * @param TemplatesName 模板名
     * @return
     */
    private String createTemplates(Map<String,Object> dataMap, String TemplatesName){
        Context context = new Context();
        context.setVariables(dataMap);
        String emailText = templateEngine.process(TemplatesName,context);
        return emailText;
    }
}
