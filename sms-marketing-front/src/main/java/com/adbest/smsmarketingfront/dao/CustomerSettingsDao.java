package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.CustomerSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CustomerSettingsDao extends JpaRepository<CustomerSettings, Long>, JpaSpecificationExecutor<CustomerSettings> {

    CustomerSettings findFirstByCustomerId(Long customerId);

    List<CustomerSettings> findByCustomerIdInAndCallReminder(List<Long> customerId, Boolean callReminder);

    List<CustomerSettings> findByCustomerId(Long id);
}
