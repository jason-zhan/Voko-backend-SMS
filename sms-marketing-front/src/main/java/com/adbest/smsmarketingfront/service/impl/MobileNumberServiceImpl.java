package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.MobileNumber;
import com.adbest.smsmarketingfront.dao.MobileNumberDao;
import com.adbest.smsmarketingfront.entity.vo.MobileNumberVo;
import com.adbest.smsmarketingfront.service.MobileNumberService;
import com.adbest.smsmarketingfront.util.Current;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MobileNumberServiceImpl implements MobileNumberService {

    @Autowired
    private MobileNumberDao mobileNumberDao;


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
}
