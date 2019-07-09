package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.CustomerMarketSetting;
import com.adbest.smsmarketingentity.MarketSetting;
import com.adbest.smsmarketingfront.dao.MarketSettingDao;
import com.adbest.smsmarketingfront.entity.vo.MarketSettingVo;
import com.adbest.smsmarketingfront.service.CustomerMarketSettingService;
import com.adbest.smsmarketingfront.service.MarketSettingService;
import com.adbest.smsmarketingfront.util.Current;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class MarketSettingServiceImpl implements MarketSettingService {

    @Autowired
    private MarketSettingDao marketSettingDao;

    @Autowired
    private CustomerMarketSettingService customerMarketSettingService;

    @Override
    public List<MarketSetting> findAll() {
        return marketSettingDao.findAll();
    }

    @Override
    public MarketSettingVo list() {
        Long customeId = Current.get().getId();
        List<MarketSetting> marketSettings = marketSettingDao.findAll();
        MarketSettingVo vo = new MarketSettingVo();
        vo.setMarketSettings(marketSettings);
        CustomerMarketSetting customerMarketSetting = customerMarketSettingService.findByCustomerId(customeId);
        vo.setCustomerMarketSetting(customerMarketSetting);
        return vo;
    }

    @Override
    public MarketSetting findById(Long id) {
        Optional<MarketSetting> optional = marketSettingDao.findById(id);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    @Override
    public Long count() {
        return marketSettingDao.count();
    }

    @Override
    @Transactional
    public void save(MarketSetting marketSetting) {
        marketSettingDao.save(marketSetting);
    }

    @Override
    public List<MarketSetting> findByPrice(BigDecimal price) {
        return marketSettingDao.findByPrice(price);
    }
}
