package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MessageRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MessageRecordDao extends JpaRepository<MessageRecord, Long>, JpaSpecificationExecutor<MessageRecord> {
    
    int deleteByPlanId(Long planId);
    
    @Transactional
    @Modifying
    @Query("update MessageRecord set status = ?2 where sid = ?1")
    int updateStatusBySid(String sid, int status);
    
    @Transactional
    @Modifying
    @Query("update MessageRecord set returnCode = ?2 where sid = ?1")
    int updateReturnCodeBySid(String sid, int returnCode);
    
    @Transactional
    @Modifying
    @Query("update MessageRecord set disable = ?2 where id = ?1")
    int disableById(Long id, boolean disable);
    
    @Transactional
    @Modifying
    @Query("update MessageRecord set status = ?2 where id = ?1")
    int updateStatusById(Long id, int status);
    
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update MessageRecord set status = ?3 where id = ?1 and customerId = ?2 and inbox = true")
    int updateStatusAfterReadMessage(Long id, Long customerId, int status);
    
    @Transactional
    @Modifying
    @Query("update MessageRecord set sid = ?2, status = ?3, sendTime = current_timestamp where id = ?1")
    int updateStatusAfterSendMessage(Long id, String sid, int status);
    
    // 根据计划id统计实际发送消息条数
    @Query("select sum(segments) from MessageRecord where planId = ?1")
    int sumMsgNumByPlanId(Long planId);
    
    MessageRecord findByIdAndCustomerIdAndDisableIsFalse(Long id, Long customerId);
    
    @Transactional
    @Modifying
    @Query("update MessageRecord set status = ?2 where planId = ?1 and disable = false")
    int updateStatusByPlanIdAndDisableIsFalse(Long planId, int status);
    
    long countByPlanIdAndDisableIsFalse(Long planId);
    
    long countByPlanIdAndStatusAndDisableIsFalse(Long planId, int status);
    
    Page<MessageRecord> findByPlanIdAndStatusAndDisableIsFalse(Long planId, int status, Pageable pageable);
    
}
