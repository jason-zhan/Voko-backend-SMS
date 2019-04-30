package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CustomerDao extends JpaRepository<Customer, Long> , JpaSpecificationExecutor<Customer> {
    Customer findFirstByEmailAndPassword(String username, String encrypt);

    Customer findByEmail(String s);

    Customer findFirstByUsername(String email);
}
