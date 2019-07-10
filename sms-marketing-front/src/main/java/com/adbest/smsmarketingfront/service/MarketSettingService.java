package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MarketSetting;
import com.adbest.smsmarketingfront.entity.vo.MarketSettingVo;

import java.math.BigDecimal;
import java.util.List;

public interface MarketSettingService {

    List<MarketSetting> findAll();

    MarketSettingVo list();

    MarketSetting findById(Long id);

    Long count();

    void save(MarketSetting marketSetting);

    List<MarketSetting> findByPrice(BigDecimal price);
}
