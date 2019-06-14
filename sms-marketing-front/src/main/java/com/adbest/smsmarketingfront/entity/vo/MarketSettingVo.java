package com.adbest.smsmarketingfront.entity.vo;

import com.adbest.smsmarketingentity.CustomerMarketSetting;
import com.adbest.smsmarketingentity.MarketSetting;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MarketSettingVo implements Serializable {

    private List<MarketSetting> marketSettings;

    private CustomerMarketSetting customerMarketSetting;
}
