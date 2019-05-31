package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.CustomerSettings;
import com.adbest.smsmarketingentity.MobileNumber;
import com.adbest.smsmarketingfront.dao.MobileNumberDao;
import com.adbest.smsmarketingfront.entity.vo.MobileNumberVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.CustomerSettingsService;
import com.adbest.smsmarketingfront.service.MobileNumberService;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import com.adbest.smsmarketingfront.util.twilio.TwilioUtil;
import com.twilio.base.ResourceSet;
import com.twilio.rest.api.v2010.account.IncomingPhoneNumber;
import com.twilio.rest.api.v2010.account.availablephonenumbercountry.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MobileNumberServiceImpl implements MobileNumberService {

    @Autowired
    private MobileNumberDao mobileNumberDao;

    @Autowired
    private TwilioUtil twilioUtil;

    @Autowired
    private ReturnMsgUtil returnMsgUtil;

    @Autowired
    private CustomerSettingsService customerSettingsService;

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
        List<MobileNumber> mobileNumbers = mobileNumberDao.findByCustomerId(customerId);
        List<MobileNumberVo> list = mobileNumbers.stream().map(s -> new MobileNumberVo(s.getId(),s.getNumber())).collect(Collectors.toList());
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
        CustomerSettings customerSettings = customerSettingsService.findByCustomerId(id);
        ServiceException.isTrue(!customerSettings.getNumberReceivingStatus(), returnMsgUtil.msg("FREE_NUMBER_UPPER_LIMIT"));
        ResourceSet<Local> resourceSet = twilioUtil.fetchNumbersByAreaCode(null);
        Iterator<Local> iterator = resourceSet.iterator();
        Local next = iterator.next();
        String phone = next.getPhoneNumber().getEndpoint();
        IncomingPhoneNumber incomingPhoneNumber = twilioUtil.purchaseNumber(phone);
        MobileNumber mobileNumber = new MobileNumber();
        mobileNumber.setCustomerId(id);
        mobileNumber.setDisable(false);
        mobileNumber.setNumber(incomingPhoneNumber.getPhoneNumber().getEndpoint());
        mobileNumberDao.save(mobileNumber);
        customerSettings.setNumberReceivingStatus(true);
        customerSettingsService.save(customerSettings);
        return new MobileNumberVo(mobileNumber.getId(), mobileNumber.getNumber());
    }
}
