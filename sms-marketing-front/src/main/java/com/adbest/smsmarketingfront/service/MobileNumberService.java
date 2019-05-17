package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MobileNumber;

import java.util.List;

public interface MobileNumberService {
    MobileNumber save(MobileNumber mobileNumber);

    List<MobileNumber> findByNumberAndDisable(String phone, boolean disable);
}
