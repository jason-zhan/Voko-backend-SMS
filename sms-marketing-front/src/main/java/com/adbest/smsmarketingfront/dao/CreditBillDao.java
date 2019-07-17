package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.CreditBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface CreditBillDao extends JpaRepository<CreditBill, Long> {

    @Query("select coalesce(sum(amount),0) from CreditBill where type = ?1 and referId = ?2")
    BigDecimal sumAmountByTypeAndReferId(int type, Long referId);
}
