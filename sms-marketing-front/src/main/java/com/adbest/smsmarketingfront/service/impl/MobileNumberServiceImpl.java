package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.CustomerSettings;
import com.adbest.smsmarketingentity.MobileNumber;
import com.adbest.smsmarketingfront.dao.MobileNumberDao;
import com.adbest.smsmarketingfront.entity.enums.RedisKey;
import com.adbest.smsmarketingfront.entity.form.SearchTwilioForm;
import com.adbest.smsmarketingfront.entity.vo.MobileNumberVo;
import com.adbest.smsmarketingfront.entity.vo.TwilioPhoneVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.CustomerSettingsService;
import com.adbest.smsmarketingfront.service.FinanceBillComponent;
import com.adbest.smsmarketingfront.service.MobileNumberService;
import com.adbest.smsmarketingfront.service.PaymentComponent;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import com.adbest.smsmarketingfront.util.TimeTools;
import com.adbest.smsmarketingfront.util.twilio.TwilioUtil;
import com.twilio.base.ResourceSet;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.IncomingPhoneNumber;
import com.twilio.rest.api.v2010.account.availablephonenumbercountry.Local;
import com.twilio.rest.api.v2010.account.availablephonenumbercountry.LocalReader;
import com.twilio.rest.api.v2010.account.availablephonenumbercountry.TollFree;
import com.twilio.rest.api.v2010.account.availablephonenumbercountry.TollFreeReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MobileNumberServiceImpl implements MobileNumberService {

    @Autowired
    private MobileNumberDao mobileNumberDao;

    @Autowired
    private TwilioUtil twilioUtil;

    @Autowired
    private ReturnMsgUtil returnMsgUtil;

    @Autowired
    private CustomerSettingsService customerSettingsService;

    @Autowired
    private PaymentComponent paymentComponent;

    @Value("${mobilePrice.free}")
    private BigDecimal freeMobilePrice;
    @Value("${mobilePrice.ordinary}")
    private BigDecimal ordinaryMobilePrice;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional
    public MobileNumber save(MobileNumber mobileNumber) {
        return mobileNumberDao.save(mobileNumber);
    }

    @Override
    public List<MobileNumber> findByNumberAndDisable(String phone, boolean disable) {
        return mobileNumberDao.findByNumberAndDisable(phone, disable);
    }

    @Override
    public List<MobileNumberVo> findAll() {
        Long customerId = Current.get().getId();
        List<MobileNumber> mobileNumbers = mobileNumberDao.findByCustomerIdAndDisable(customerId, false);
        List<MobileNumberVo> list = mobileNumbers.stream().map(s -> new MobileNumberVo(s)).collect(Collectors.toList());
        return list;
    }

    @Override
    public List<MobileNumber> findByCustomerIdInAndDisable(List<Long> customerId, boolean disable) {
        return mobileNumberDao.findByCustomerIdInAndDisable(customerId, disable);
    }

    @Override
    @Transactional
    public MobileNumberVo init() {
        Long id = Current.get().getId();
        Boolean is = redisTemplate.opsForValue().setIfAbsent(RedisKey.INIT_PHONE.getKey() + id, System.currentTimeMillis(), RedisKey.INIT_PHONE.getExpireTime(), RedisKey.INIT_PHONE.getTimeUnit());
        ServiceException.isTrue(is,returnMsgUtil.msg("CLICK_FREQUENTLY"));
        CustomerSettings customerSettings = customerSettingsService.findByCustomerId(id);
        ServiceException.isTrue(!customerSettings.getNumberReceivingStatus(), returnMsgUtil.msg("FREE_NUMBER_UPPER_LIMIT"));
        ResourceSet<TollFree> tollFrees = twilioUtil.fetchTollFreeNumbers();
        Iterator<TollFree> iterator = tollFrees.iterator();
        TollFree next = iterator.next();
        String phone = next.getPhoneNumber().getEndpoint();
        IncomingPhoneNumber incomingPhoneNumber = twilioUtil.purchaseNumber(phone);
        MobileNumber mobileNumber = new MobileNumber();
        mobileNumber.setCustomerId(id);
        mobileNumber.setDisable(false);
        mobileNumber.setNumber(incomingPhoneNumber.getPhoneNumber().getEndpoint());
        mobileNumber.setMms(incomingPhoneNumber.getCapabilities().getMms());
        mobileNumber.setSms(incomingPhoneNumber.getCapabilities().getSms());
        mobileNumber.setGiftNumber(true);
        mobileNumber.setSid(incomingPhoneNumber.getSid());
        mobileNumberDao.save(mobileNumber);
        customerSettings.setNumberReceivingStatus(true);
        customerSettingsService.save(customerSettings);
        return new MobileNumberVo(mobileNumber);
    }

    @Override
    public Long countByDisableAndCustomerIdAndGiftNumber(Boolean disable, Long customerId, Boolean giftNumber) {
        return mobileNumberDao.countByDisableAndCustomerIdAndGiftNumber(disable, customerId, giftNumber);
    }

    @Override
    public List<MobileNumber> findInvalidMobile(Timestamp time) {
        return mobileNumberDao.findInvalidMobile(time);
    }

    @Override
    @Transactional
    public boolean delete(MobileNumber mobileNumber) {
        boolean is = twilioUtil.deletePhoneNumber(mobileNumber.getSid());
        if (is){
            mobileNumber.setDisable(true);
            mobileNumberDao.save(mobileNumber);
        }
        return is;
    }

    @Override
    public List<MobileNumber> findByGiftNumberAndDisableAndInvalidTimebefore(Boolean giftNumber, Boolean disable, Timestamp time) {
        return mobileNumberDao.findByGiftNumberAndDisableAndInvalidTimeBefore(giftNumber, disable, time);
    }

    @Override
    public void saveAll(List<MobileNumber> renewMobileNumber) {
        mobileNumberDao.saveAll(renewMobileNumber);
    }

    @Override
    public List<MobileNumber> findByGiftNumberAndDisableAndInvalidTimebeforeAndAutomaticRenewal(Boolean giftNumber, Boolean disable, Timestamp time, Boolean automaticRenewal) {
        return mobileNumberDao.findByGiftNumberAndDisableAndInvalidTimeBeforeAndAutomaticRenewal(giftNumber, disable, time, automaticRenewal);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        Long customerId = Current.get().getId();
        Optional<MobileNumber> optional = mobileNumberDao.findById(id);
        ServiceException.isTrue(optional.isPresent(),returnMsgUtil.msg("PHONE_NOT_EXIST"));
        MobileNumber mobileNumber = optional.get();
        ServiceException.isTrue(mobileNumber.getCustomerId().longValue()==customerId,returnMsgUtil.msg("PHONE_NOT_EXIST"));
        boolean is = twilioUtil.deletePhoneNumber(mobileNumber.getSid());
        if (is){
            mobileNumber.setDisable(true);
            mobileNumberDao.save(mobileNumber);
        }
        return is;
    }

    @Override
    @Transactional
    public boolean automaticRenewal(Long id, Boolean automaticRenewal) {
        Long customerId = Current.get().getId();
        ServiceException.notNull(automaticRenewal, returnMsgUtil.msg("Renewal_status_NOT_EMPTY"));
        Optional<MobileNumber> optional = mobileNumberDao.findById(id);
        ServiceException.isTrue(optional.isPresent(),returnMsgUtil.msg("PHONE_NOT_EXIST"));
        MobileNumber mobileNumber = optional.get();
        ServiceException.isTrue(mobileNumber.getCustomerId().longValue()==customerId,returnMsgUtil.msg("PHONE_NOT_EXIST"));
        mobileNumber.setAutomaticRenewal(automaticRenewal);
        mobileNumberDao.save(mobileNumber);
        return false;
    }

    @Override
    public List<TwilioPhoneVo> search(SearchTwilioForm searchTwilioForm) {
        LocalReader us = Local.reader("US")
                .setSmsEnabled(true)
                .setMmsEnabled(true);
        if (!StringUtils.isEmpty(searchTwilioForm.getAreaCode())){
            try {
                us.setAreaCode(Integer.valueOf(searchTwilioForm.getAreaCode()));
            }catch (NumberFormatException e){
                ServiceException.isTrue(false, returnMsgUtil.msg("AREA_NUMBER_FORMAT_ERROR"));
            }
        }
        if (!StringUtils.isEmpty(searchTwilioForm.getContains())){
            us.setContains(searchTwilioForm.getContains());
        }
        if (!StringUtils.isEmpty(searchTwilioForm.getInRegion())){
            us.setInRegion(searchTwilioForm.getInRegion());
        }
        ResourceSet<Local> read = us.read();
        List<TwilioPhoneVo> vos = new ArrayList<>();
        Iterator<Local> iterator = read.iterator();
        while (iterator.hasNext()){
            vos.add(new TwilioPhoneVo(iterator.next()));
        }
        return vos;
    }

    @Override
    public List<TwilioPhoneVo> tollFreeSearch(SearchTwilioForm searchTwilioForm) {
        TollFreeReader tollFreeReader = TollFree.reader("US")
                .setSmsEnabled(true);
        if (!StringUtils.isEmpty(searchTwilioForm.getContains())){
            tollFreeReader.setContains(searchTwilioForm.getContains());
        }
        if (!StringUtils.isEmpty(searchTwilioForm.getAreaCode())){
            try {
                tollFreeReader.setAreaCode(Integer.valueOf(searchTwilioForm.getAreaCode()));
            }catch (NumberFormatException e){
                ServiceException.isTrue(false, returnMsgUtil.msg("AREA_NUMBER_FORMAT_ERROR"));
            }
        }
        ResourceSet<TollFree> read = tollFreeReader.read();
        List<TwilioPhoneVo> vos = new ArrayList<>();
        Iterator<TollFree> iterator = read.iterator();
        while (iterator.hasNext()){
            vos.add(new TwilioPhoneVo(iterator.next()));
        }
        return vos;
    }

    @Override
    @Transactional
    public boolean buyPhone(String phoneNumber, Boolean automaticRenewal) {
        ServiceException.hasText(phoneNumber, returnMsgUtil.msg("PHONE_NOT_EMPTY"));
        BigDecimal price = null;
        if (phoneNumber.startsWith("+18")){
            price = freeMobilePrice;
        }else{
            price = ordinaryMobilePrice;
        }
        Long customerId = Current.get().getId();
        /**
         * 扣钱，账单
         */
        paymentComponent.realTimePayment(customerId, price.negate(),returnMsgUtil.msg("MOBILE_PURCHASE"));

        IncomingPhoneNumber incomingPhoneNumber = null;
        try {
            incomingPhoneNumber = twilioUtil.purchaseNumber(phoneNumber);
        }catch (ApiException e){
            log.error("Error in purchasing mobile phone number，{}",e);
            ServiceException.isTrue(false, returnMsgUtil.msg("MOBILE_NOT_BUY"));
        }
        MobileNumber mobileNumber = new MobileNumber();
        mobileNumber.setDisable(false);
        mobileNumber.setCustomerId(customerId);
        mobileNumber.setNumber(incomingPhoneNumber.getPhoneNumber().getEndpoint());
        mobileNumber.setMms(incomingPhoneNumber.getCapabilities().getMms());
        mobileNumber.setSms(incomingPhoneNumber.getCapabilities().getSms());
        mobileNumber.setGiftNumber(false);
        mobileNumber.setInvalidTime(TimeTools.addDay(TimeTools.now(),30));
        mobileNumber.setAutomaticRenewal(automaticRenewal==null?false:automaticRenewal);
        mobileNumber.setSid(incomingPhoneNumber.getSid());
        mobileNumberDao.save(mobileNumber);
        return true;
    }
}
