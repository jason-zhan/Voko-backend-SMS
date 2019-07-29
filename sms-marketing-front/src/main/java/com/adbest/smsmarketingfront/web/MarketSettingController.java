package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingentity.CustomerMarketSetting;
import com.adbest.smsmarketingentity.MarketSetting;
import com.adbest.smsmarketingfront.entity.form.KeywordForm;
import com.adbest.smsmarketingfront.entity.vo.CustomerMarketSettingVo;
import com.adbest.smsmarketingfront.entity.vo.KeywordVo;
import com.adbest.smsmarketingfront.entity.vo.MarketSettingVo;
import com.adbest.smsmarketingfront.service.CustomerMarketSettingService;
import com.adbest.smsmarketingfront.service.MarketSettingService;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/marketSetting")
public class MarketSettingController {

    @Autowired
    private MarketSettingService marketSettingService;

    @Autowired
    private CustomerMarketSettingService customerMarketSettingService;

    @RequestMapping("/list")
    public ReturnEntity list(){
        Long customerId = Current.get().getId();
        MarketSettingVo vo = marketSettingService.list();
        CustomerMarketSettingVo customerMarketSetting = customerMarketSettingService.details();
        vo.setCustomerMarketSetting(customerMarketSetting);
        return ReturnEntity.success(vo);
    }
}
