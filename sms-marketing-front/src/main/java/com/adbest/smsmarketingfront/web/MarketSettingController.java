package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingentity.MarketSetting;
import com.adbest.smsmarketingfront.entity.form.KeywordForm;
import com.adbest.smsmarketingfront.entity.vo.KeywordVo;
import com.adbest.smsmarketingfront.entity.vo.MarketSettingVo;
import com.adbest.smsmarketingfront.service.MarketSettingService;
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

    @RequestMapping("/list")
    public ReturnEntity list(){
        MarketSettingVo vo = marketSettingService.list();
        return ReturnEntity.success(vo);
    }
}
