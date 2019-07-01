package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.FinanceBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.sql.Timestamp;

public interface FinanceBillDao extends JpaRepository<FinanceBill, Long> {
    
    // 根据用户id与时间区间统计
    @Query("select coalesce(sum(amount),0) from FinanceBill where customerId = ?1 and time between ?2 and ?3")
    BigDecimal sumWithCustomerIdAndTimeRange(Long customerId, Timestamp from, Timestamp to);
    
    // 根据用户id账单状态统计
    @Query("select coalesce(sum(amount),0) from FinanceBill where customerId = ?1 and status = ?2")
    BigDecimal sumWithCustomerAndStatus(Long customerId, int status);
}
