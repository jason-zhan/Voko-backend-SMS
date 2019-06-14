package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.CustomerMarketSetting;
import com.adbest.smsmarketingfront.entity.vo.CustomerMarketSettingVo;

import java.sql.Timestamp;
import java.util.List;

public interface CustomerMarketSettingService {
    CustomerMarketSetting save(CustomerMarketSetting customerMarketSetting);

    CustomerMarketSettingVo details();

    CustomerMarketSettingVo introduce();

    CustomerMarketSetting findByCustomerId(Long customerId);

    CustomerMarketSettingVo buy(Long id, Boolean automaticRenewal);

    List<CustomerMarketSetting> findByInvalidStatusAndInvalidTimeBeforeAndAutomaticRenewal(Boolean invalidStatus, Timestamp now, Boolean automaticRenewal);

    List<CustomerMarketSetting> findByInvalidStatusAndInvalidTimeBefore(Boolean invalidStatus, Timestamp now);

    void saveAll(List<CustomerMarketSetting> list);

    CustomerMarketSettingVo automaticRenewal(Boolean automaticRenewal);
}
