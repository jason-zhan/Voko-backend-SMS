package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.CreditBill;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface CreditBillDao extends JpaRepository<CreditBill, Long> {

    @Query("select coalesce(sum(amount),0) from CreditBill where type = ?1 and referId = ?2")
    BigDecimal sumAmountByTypeAndReferId(int type, Long referId);

    @Query("select distinct customerId from CreditBill where chargingStatus in ?1 and type in ?2")
    List<Long> findPushUsers(List<Integer> creditBillChargingStatus, List<Integer> creditBillTypes, Pageable pageable);

    List<CreditBill> findByTypeInAndChargingStatusInAndCustomerIdIn(List<Integer> creditBillTypes, List<Integer> creditBillChargingStatus, List<Long> customers);
}
