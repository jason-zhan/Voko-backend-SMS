package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MarketSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketSettingDao extends JpaRepository<MarketSetting, Long> {
}
