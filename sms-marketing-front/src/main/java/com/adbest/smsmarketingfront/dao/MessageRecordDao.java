package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.entity.vo.OutboxReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;


import java.util.List;

public interface MessageRecordDao extends JpaRepository<MessageRecord, Long>, JpaSpecificationExecutor<MessageRecord> {
    
    @Transactional
    @Modifying
    @Query("update MessageRecord set returnCode = ?2, status = ?3 where sid = ?1 and planId is not null")
    int updateReturnCodeAndStatusBySid(String sid, int returnCode, int status);
    
    @Transactional
    @Modifying
    @Query("update MessageRecord set returnCode = ?2, status = ?3, arrivedTime = current_timestamp where sid = ?1 and planId is not null ")
    int updateMsgForDelivered(String sid, int deliveredCode, int deliveredStatus);
    
    @Transactional
    @Modifying
    @Query("update MessageRecord set returnCode = ?2 where sid = ?1 and planId is not null")
    int updateReturnCodeBySid(String sid, int returnCode);
    
    @Transactional
    @Modifying
    @Query("update MessageRecord set disable = ?3 where id = ?1 and customerId = ?2")
    int disableByIdAndCustomerId(Long id, Long customerId, boolean disable);
    
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update MessageRecord set status = ?3 where id = ?1 and customerId = ?2 and inbox = true")
    int updateStatusAfterReadMessage(Long id, Long customerId, int status);
    
    @Transactional
    @Modifying
    @Query("update MessageRecord set sid = ?2, status = ?3, sendTime = current_timestamp where id = ?1")
    int updateStatusAfterSendMessage(Long id, String sid, int status);
    
    
    @Transactional
    @Modifying
    @Query("update MessageRecord set status = ?2 where planId = ?1 and disable = false")
    int updateStatusByPlanIdAndDisableIsFalse(Long planId, int status);
    
    MessageRecord findByIdAndCustomerIdAndDisableIsFalse(Long id, Long customerId);
    
    boolean existsByPlanId(Long planId);
    
    boolean existsByPlanIdAndStatus(Long planId, int status);
    
    int countByPlanIdAndStatus(Long planId, int status);
    
    @Query("select coalesce(sum(segments),0) from MessageRecord where planId = ?1 and status = ?2")
    int sumMsgByPlanIdAndStatus(Long planId, int status);
    
    Page<MessageRecord> findByPlanIdAndStatusAndDisableIsFalse(Long planId, int status, Pageable pageable);

    List<MessageRecord> findByReturnCodeAndDisableAndPlanIdIsNull(Integer returnCode, Boolean disable, Pageable pageable);



    @Query("select new com.adbest.smsmarketingfront.entity.vo.OutboxReport(date_format(c.sendTime, '%Y-%m-%d'),count(distinct c.id)) from MessageRecord c \n" +
            "where c.sendTime > ?1 and c.sendTime < ?2 and c.customerId = ?3 \n" +
            "group by date_format(c.sendTime, '%Y-%m-%d')")
    List<OutboxReport> findGroupBySendTimeAndCustomerId(Timestamp startD, Timestamp endD, Long customerId, Pageable pageable);
}
