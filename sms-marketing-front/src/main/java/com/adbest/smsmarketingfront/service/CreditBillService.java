package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.CreditBill;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CreditBillService {
    List<Long> findPushUsers(List<Integer> creditBillChargingStatus, List<Integer> creditBillTypes, Pageable pageable);

    List<CreditBill> findByTypeInAndChargingStatusInAndCustomerIdIn(List<Integer> creditBillTypes, List<Integer> creditBillChargingStatus, List<Long> customers);
}
