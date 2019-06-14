package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.CustomerMarketSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;

public interface CustomerMarketSettingDao extends JpaRepository<CustomerMarketSetting, Long> {
    List<CustomerMarketSetting> findByCustomerId(Long customerId);

    List<CustomerMarketSetting> findByInvalidStatusAndInvalidTimeBeforeAndAutomaticRenewal(Boolean invalidStatus, Timestamp now, Boolean automaticRenewal);

    List<CustomerMarketSetting> findByInvalidStatusAndInvalidTimeBefore(Boolean invalidStatus, Timestamp now);
}
