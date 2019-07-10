package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingfront.entity.vo.CustomerMarketSettingVo;
import com.adbest.smsmarketingfront.service.CustomerMarketSettingService;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer/plans")
public class CustomerMarketSettingController {

    @Autowired
    private CustomerMarketSettingService customerMarketSettingService;

    @RequestMapping("/details")
    public ReturnEntity details(){
        CustomerMarketSettingVo vo = customerMarketSettingService.details();
        return ReturnEntity.success(vo);
    }

    @RequestMapping("/introduce")
    public ReturnEntity introduce(){
        CustomerMarketSettingVo vo = customerMarketSettingService.introduce();
        return ReturnEntity.success(vo);
    }

    @RequestMapping("/buy")
    public ReturnEntity buy(Long id, Boolean automaticRenewal){
        CustomerMarketSettingVo vo = customerMarketSettingService.buy(id, automaticRenewal);
        return ReturnEntity.success(vo);
    }

    @RequestMapping("/automaticRenewal")
    public ReturnEntity automaticRenewal(Boolean automaticRenewal){
        CustomerMarketSettingVo vo = customerMarketSettingService.automaticRenewal(automaticRenewal);
        return ReturnEntity.success(vo);
    }
}
