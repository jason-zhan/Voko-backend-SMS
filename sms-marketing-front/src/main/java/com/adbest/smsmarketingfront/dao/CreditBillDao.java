package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.CreditBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface CreditBillDao extends JpaRepository<CreditBill, Long> {
    
    @Query("select coalesce(sum(cb.amount),0)+cr.credit from CreditBill cb left join Customer cr on cr.id = cb.customerId where customerId = ?1")
    BigDecimal sumAmountByCustomerId(Long customerId);
}
