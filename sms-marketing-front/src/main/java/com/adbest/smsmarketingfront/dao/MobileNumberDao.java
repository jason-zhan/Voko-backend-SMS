package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MobileNumber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MobileNumberDao extends JpaRepository<MobileNumber,Long> {
    List<MobileNumber> findByNumberAndDisable(String phone, boolean disable);
}
