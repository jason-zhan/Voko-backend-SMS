package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface CustomerDao extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
    
    @Transactional
    @Modifying
    @Query("update Customer set availableCredit = availableCredit+?2 where id = ?1 and availableCredit+?2 >= 0")
    int updateCredit(Long customerId, BigDecimal cost);
    
    @Transactional
    @Modifying
    @Query("update Customer set availableCredit = availableCredit+?2, maxCredit = maxCredit+?2 where id = ?1 and maxCredit+?2 >= 0")
    int updateMaxCredit(Long customerId, BigDecimal amount);
    
    Customer findFirstByEmailAndPassword(String username, String encrypt);
    
    Customer findByEmail(String s);
    
    Customer findFirstByEmail(String email);
    
    List<Customer> findByEmailIn(List<String> emails);
}
