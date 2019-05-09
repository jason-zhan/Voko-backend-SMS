package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MobileNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MbNumberLibDao extends JpaRepository<MobileNumber, Long>, JpaSpecificationExecutor<MobileNumber> {

}
