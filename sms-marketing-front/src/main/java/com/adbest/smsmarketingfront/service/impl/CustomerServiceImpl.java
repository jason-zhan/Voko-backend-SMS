package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.entity.dto.CustomerDto;
import com.adbest.smsmarketingfront.entity.enums.CustomerSource;
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
import org.springframework.data.redis.core.StringRedisTemplate;
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
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Autowired
    private CustomerSettingsService customerSettingsService;

    @Autowired
    private VkCustomersService vkCustomersService;

    @Autowired
    private CustomerMarketSettingService customerMarketSettingService;

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
    public CustomerVo register(CustomerForm createSysUser, HttpServletRequest request) {

        String sessionId = request.getSession().getId();
        ServiceException.hasText(createSysUser.getCode(), returnMsgUtil.msg("CODE_NOT_EMPTY"));
        Object rCode = redisTemplate.opsForValue().get("code:"+sessionId);
        ServiceException.notNull(rCode, returnMsgUtil.msg("VERIFICATION_INFO_EXPIRED"));
        ServiceException.isTrue(createSysUser.getCode().equals(rCode), returnMsgUtil.msg("VERIFICATION_CODE_ERROR"));
        redisTemplate.delete("code:"+sessionId);

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
        customer.setSource(CustomerSource.REGISTER.getValue());
        String realIp = getRealIp(request);
        String key = "register:" + realIp;
        Boolean is = redisTemplate.opsForValue().setIfAbsent(key, "1", 60*60, TimeUnit.SECONDS);
        if (!is){
            Object count = redisTemplate.opsForValue().get(key);
            if (count==null){
                redisTemplate.opsForValue().setIfAbsent(key, "1", 1, TimeUnit.SECONDS);
            }else {
                Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                ServiceException.isTrue(Long.valueOf(count.toString())<=5, returnMsgUtil.msg("EXCEED_MAX_REGISTRATIONS"));
                redisTemplate.opsForValue().set(key, Long.valueOf(count.toString())+1, expire>0?expire:60*60, TimeUnit.SECONDS);
            }
        }
        customerDao.save(customer);

        CustomerSettings customerSettings = new CustomerSettings(false, customer.getId(), false);
        customerSettingsService.save(customerSettings);
//        new Thread(){
//            public void run() {
//                initCustomerData(customer);
//            }
//        }.start();
        initCustomerData(customer);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(customer.getEmail(), createSysUser.getPassword());
        Authentication authenticatedUser = authenticationManager
        .authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
        return new CustomerVo(customer);
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
        List<MarketSetting> marketSettings = marketSettingService.findAll();
        if (marketSettings.size()<=0)return;
        MarketSetting marketSetting = marketSettings.get(0);
        //初始化关键字
//        List<Keyword> keywords = new ArrayList<>();
//        Keyword keyword = null;
//        if (marketSetting.getKeywordTotal()>0){
//            for (int i = 0;i<marketSetting.getKeywordTotal();i++){
//                keyword = new Keyword(customer.getId(), UUID.randomUUID().toString().replaceAll("-",""));
//                keywords.add(keyword);
//            }
//            keywordService.saveAll(keywords);
//        }
        //初始化套餐
        CustomerMarketSetting customerMarketSetting = new CustomerMarketSetting(marketSetting);
        customerMarketSetting.setOrderTime(TimeTools.now());
        customerMarketSetting.setInvalidTime(TimeTools.addDay(TimeTools.now(),marketSetting.getDaysNumber()));
        customerMarketSetting.setCustomerId(customer.getId());
        customerMarketSetting.setAutomaticRenewal(false);
        customerMarketSetting.setInvalidStatus(false);
        customerMarketSettingService.save(customerMarketSetting);

        String infoDescribe ="experience gift";
        //初始化短信条数
        if (marketSetting.getSmsTotal()>0){
            SmsBill smsBill = new SmsBill(customer.getId(),infoDescribe,marketSetting.getSmsTotal());
            smsBillComponent.save(smsBill);
        }
        //初始化彩信条数
        if (marketSetting.getMmsTotal()>0){
            MmsBill mmsBill = new MmsBill(customer.getId(),infoDescribe,marketSetting.getMmsTotal());
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
    public CustomerVo updateInfo(CustomerForm customerForm) {
        Long customerId = Current.get().getId();
        Customer customer = customerDao.findById(customerId).get();
        customer.setOrganization(customerForm.getOrganization());
        customer.setIndustry(customerForm.getIndustry());
        customer.setLastName(customerForm.getLastName());
        customer.setFirstName(customerForm.getFirstName());
        customerDao.save(customer);
        return new CustomerVo(customer);
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
        if (customer.getFirstName()!=null){
            dataMap.put("name",customer.getFirstName());
        }
        String emailText = createTemplates(dataMap,"email-code-templates");
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
        redisTemplate.delete("login:"+customer.getEmail());
        return true;
    }

    @Override
    public BufferedImage createVerifyCode(HttpServletRequest request) {
        int width = 115;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        Random rdm = new Random();
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        String verifyCode = getNumber(5);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 32));
        g.drawString(verifyCode, 15, 28);
        g.dispose();
        redisTemplate.opsForValue().set("code:"+request.getSession().getId(),verifyCode,3, TimeUnit.MINUTES);
        return image;
    }

    @Override
    public Customer findById(Long customerId) {
        Optional<Customer> optional = customerDao.findById(customerId);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    @Override
    @Transactional
    public void saveImportCustomer(List<Customer> customerList) {
        customerDao.saveAll(customerList);
        CustomerSettings customerSettings = null;
        List<CustomerSettings> customerSettingsList = new ArrayList<>();
        List<SmsBill> smsBills = new ArrayList<>();
        List<MmsBill> mmsBills = new ArrayList<>();
        MmsBill mmsBill = null;
        SmsBill smsBill = null;
        List<MarketSetting> marketSettings = marketSettingService.findAll();
        MarketSetting marketSetting = marketSettings.get(0);
        String infoDescribe ="experience gift";
        for (Customer c:customerList) {
            customerSettings = new CustomerSettings(false, c.getId(), false);
            customerSettingsList.add(customerSettings);
            if (marketSetting.getMmsTotal()>0){
                mmsBill = new MmsBill(c.getId(),infoDescribe,marketSetting.getMmsTotal());
                mmsBills.add(mmsBill);
            }
            if (marketSetting.getSmsTotal()>0){
                smsBill = new SmsBill(c.getId(),infoDescribe,marketSetting.getSmsTotal());
                smsBills.add(smsBill);
            }
        }
        customerSettingsService.saveAll(customerSettingsList);
        if (smsBills.size()>0){smsBillComponent.saveAll(smsBills);}
        if (mmsBills.size()>0){mmsBillComponent.saveAll(mmsBills);}
        vkCustomersService.updateInLeadinByEmailIn(true, customerList.stream().map(c -> c.getEmail()).collect(Collectors.toList()));
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

    private String getRealIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if(!StringUtils.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)){
            int index = ip.indexOf(",");
            if(index != -1){
                return ip.substring(0,index);
            }else{
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if(!StringUtils.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)){
            return ip;
        }
        return request.getRemoteAddr();
    }
}
