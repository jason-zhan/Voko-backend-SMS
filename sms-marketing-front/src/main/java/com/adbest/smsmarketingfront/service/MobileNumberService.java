package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MobileNumber;
import com.adbest.smsmarketingfront.entity.form.SearchTwilioForm;
import com.adbest.smsmarketingfront.entity.vo.MobileNumberVo;
import com.adbest.smsmarketingfront.entity.vo.TwilioPhoneVo;

import java.util.List;

public interface MobileNumberService {
    MobileNumber save(MobileNumber mobileNumber);

    List<MobileNumber> findByNumberAndDisable(String phone, boolean disable);

    List<MobileNumberVo> findAll();

    List<MobileNumber> findByCustomerIdInAndDisable(List<Long> customerId, boolean disable);

    MobileNumberVo init();

    List<TwilioPhoneVo> search(SearchTwilioForm searchTwilioForm);

    List<TwilioPhoneVo> tollFreeSearch(SearchTwilioForm searchTwilioForm);

    boolean buyPhone(String phoneNumber);
}
