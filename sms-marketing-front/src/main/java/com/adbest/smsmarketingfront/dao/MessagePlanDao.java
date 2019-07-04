package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MessagePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public interface MessagePlanDao extends JpaRepository<MessagePlan, Long>, JpaSpecificationExecutor<MessagePlan> {
    
    @Transactional
    @Modifying
    @Query("update MessagePlan set status = ?2 where id = ?1")
    int updateStatusById(Long id, int status);
    
    @Transactional
    @Modifying
    @Query("update MessagePlan set disable = ?3 where id = ?1 and customerId = ?2")
    int disableByIdAndCustomerId(Long id, Long customerId, boolean disable);
    
    @Transactional
    @Modifying
    @Query("update MessagePlan set msgTotal = 0, creditPayNum = 0, creditPayCost = 0, status = ?2 where id = ?1 and status = ?3")
    int cancelMessagePlan(Long id, int targetStatus, int currentStatus);

    MessagePlan findByIdAndCustomerIdAndDisableIsFalse(Long id, Long customerId);
    
    List<MessagePlan> findByStatusAndExecTimeBeforeAndDisableIsFalse(int status, Date execTime);
}
