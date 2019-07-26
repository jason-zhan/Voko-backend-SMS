package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.CreditBill;
import com.adbest.smsmarketingfront.dao.CreditBillDao;
import com.adbest.smsmarketingfront.service.CreditBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreditBillServiceImpl implements CreditBillService {

    @Autowired
    private CreditBillDao creditBillDao;

    @Override
    public List<Long> findPushUsers(List<Integer> creditBillChargingStatus, List<Integer> creditBillTypes, Pageable pageable) {
        return creditBillDao.findPushUsers(creditBillChargingStatus, creditBillTypes, pageable);
    }

    @Override
    public List<CreditBill> findByTypeInAndChargingStatusInAndCustomerIdIn(List<Integer> creditBillTypes, List<Integer> creditBillChargingStatus, List<Long> customers) {
        return creditBillDao.findByTypeInAndChargingStatusInAndCustomerIdIn(creditBillTypes, creditBillChargingStatus, customers);
    }
}
