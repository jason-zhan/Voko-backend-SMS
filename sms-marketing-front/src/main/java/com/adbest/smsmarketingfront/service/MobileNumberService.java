package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MobileNumber;
import com.adbest.smsmarketingfront.entity.vo.MobileNumberVo;

import java.util.List;

public interface MobileNumberService {
    MobileNumber save(MobileNumber mobileNumber);

    List<MobileNumber> findByNumberAndDisable(String phone, boolean disable);

    List<MobileNumberVo> findAll();

}
