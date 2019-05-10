package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.MobileNumber;
import com.adbest.smsmarketingfront.dao.MobileNumberDao;
import com.adbest.smsmarketingfront.service.MobileNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class MobileNumberServiceImpl implements MobileNumberService {

    @Autowired
    private MobileNumberDao mobileNumberDao;


    @Override
    @Transactional
    public MobileNumber save(MobileNumber mobileNumber) {
        return mobileNumberDao.save(mobileNumber);
    }
}
