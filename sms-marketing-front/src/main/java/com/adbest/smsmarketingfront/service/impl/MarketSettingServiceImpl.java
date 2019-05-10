package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.MarketSetting;
import com.adbest.smsmarketingfront.dao.MarketSettingDao;
import com.adbest.smsmarketingfront.service.MarketSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketSettingServiceImpl implements MarketSettingService {

    @Autowired
    private MarketSettingDao marketSettingDao;

    @Override
    public List<MarketSetting> findAll() {
        return marketSettingDao.findAll();
    }
}
