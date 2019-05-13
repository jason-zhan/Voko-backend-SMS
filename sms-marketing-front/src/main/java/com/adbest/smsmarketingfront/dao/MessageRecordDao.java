package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MessageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MessageRecordDao extends JpaRepository<MessageRecord, Long>, JpaSpecificationExecutor<MessageRecord> {
    
    int deleteByPlanId(Long planId);
    
    @Transactional
    @Modifying
    @Query("update MessageRecord set disable = ?2 where id = ?1")
    int disableById(Long id, boolean disable);
    
    @Transactional
    @Modifying
    @Query("update MessageRecord set disable = ?3 where id = ?1 and customerId = ?2")
    int disableByIdAndCustomerId(Long id, Long customerId, boolean disable);
    
    // 根据计划id统计实际发送消息条数
    @Query("select sum(segments) from MessageRecord where planId = ?1")
    int sumMsgNumByPlanId(Long planId);
    
    MessageRecord findByIdAndCustomerIdAndDisableIsFalse(Long id, Long customerId);
}
