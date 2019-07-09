package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public interface CustomerDao extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
    
    @Transactional
    @Modifying
    @Query("update Customer set credit = credit+?1 where id = ?2 and credit+?1 >= 0")
    int updateCreditByCustomerId(BigDecimal cost, Long customerId);

    Customer findFirstByCustomerLogin(String username);

    List<Customer> findByCustomerLoginIn(ArrayList<String> customerLogins);

    Customer findFirstByCustomerLoginAndPassword(String username, String password);
}
