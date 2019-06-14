package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.CustomerMarketSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

public interface CustomerMarketSettingDao extends JpaRepository<CustomerMarketSetting, Long> {
    
    @Transactional
    @Modifying
    @Query("update CustomerMarketSetting set smsTotal=smsTotal+?2 where customerId = ?1 and smsTotal+?2 >= 0")
    int updateSmsByCustomerId(Long customerId, int sms);
    
    @Transactional
    @Modifying
    @Query("update CustomerMarketSetting set mmsTotal=mmsTotal+?2 where customerId = ?1 and mmsTotal+?2 >= 0")
    int updateMmsByCustomerId(Long customerId, int mms);
    
    List<CustomerMarketSetting> findByCustomerId(Long customerId);
    
    CustomerMarketSetting findFirstByCustomerId(Long customerId);
    
    List<CustomerMarketSetting> findByInvalidStatusAndInvalidTimeBeforeAndAutomaticRenewal(Boolean invalidStatus, Timestamp now, Boolean automaticRenewal);
    
    List<CustomerMarketSetting> findByInvalidStatusAndInvalidTimeBefore(Boolean invalidStatus, Timestamp now);
}
