package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MmsBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface MmsBillDao extends JpaRepository<MmsBill, Long>, JpaSpecificationExecutor<MmsBill> {
    
    MmsBill findByIdAndCustomerId(Long id, Long customerId);
    
    @Query("select coalesce(sum(amount),0) from MmsBill where customerId = ?1")
    Long sumByCustomerId(Long customerId);
}
