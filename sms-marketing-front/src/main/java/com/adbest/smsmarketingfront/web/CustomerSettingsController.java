package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingfront.entity.form.CustomerSettingsForm;
import com.adbest.smsmarketingfront.entity.vo.CustomerSettingsVo;
import com.adbest.smsmarketingfront.service.CustomerSettingsService;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer/Settings")
public class CustomerSettingsController {

    @Autowired
    private CustomerSettingsService customerSettingsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping
    public ReturnEntity get(){
        CustomerSettingsVo vo = customerSettingsService.get();
        return ReturnEntity.success(vo);
    }

    @RequestMapping("/update")
    public ReturnEntity update(CustomerSettingsForm customerSettingsForm){
        boolean is = customerSettingsService.update(customerSettingsForm);
        return ReturnEntity.success(is);
    }
}
