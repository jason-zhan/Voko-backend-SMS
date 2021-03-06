package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.SmsBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface SmsBillDao extends JpaRepository<SmsBill, Long>, JpaSpecificationExecutor<SmsBill> {
    
    SmsBill findByIdAndCustomerId(Long id, Long customerId);
    
    @Query("select coalesce(sum(amount),0) from SmsBill where customerId = ?1")
    Long sumByCustomerId(Long customerId);
    
}
