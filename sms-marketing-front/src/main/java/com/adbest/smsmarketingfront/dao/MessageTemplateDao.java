package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MessageTemplateDao extends JpaRepository<MessageTemplate, Long>, JpaSpecificationExecutor<MessageTemplate> {
    
    int deleteByIdAndCustomerId(Long id, Long customerId);
    
    @Transactional
    @Modifying
    @Query("update MessageTemplate set disable = ?3 where id = ?1 and customerId = ?2")
    int disableById(Long id, Long customerId, boolean disable);
    
    MessageTemplate findByIdAndCustomerIdAndDisableIsFalse(Long id, Long customerId);
}
