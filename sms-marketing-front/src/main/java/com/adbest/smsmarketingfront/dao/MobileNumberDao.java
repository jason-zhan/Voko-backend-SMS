package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MobileNumber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MobileNumberDao extends JpaRepository<MobileNumber,Long> {
}
