package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingfront.entity.dto.CustomerDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomerDao extends JpaRepository<Customer, Long> , JpaSpecificationExecutor<Customer> {
    Customer findFirstByEmailAndPassword(String username, String encrypt);

    Customer findByEmail(String s);

    Customer findFirstByEmail(String email);

    List<Customer> findByEmailIn(List<String> emails);
}
