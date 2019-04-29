package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerDao extends JpaRepository<Customer, Long> {
}
