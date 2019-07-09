package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MarketSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface MarketSettingDao extends JpaRepository<MarketSetting, Long> {
    List<MarketSetting> findByPrice(BigDecimal price);
}
