package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MobileNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MbNumberLibDao extends JpaRepository<MobileNumber, Long>, JpaSpecificationExecutor<MobileNumber> {

    MobileNumber findTopByCustomerIdAndNumberAndDisableIsFalse(Long customerId, String number);
}
