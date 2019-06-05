package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingentity.MobileNumber;
import com.adbest.smsmarketingfront.entity.form.SearchTwilioForm;
import com.adbest.smsmarketingfront.entity.vo.MobileNumberVo;
import com.adbest.smsmarketingfront.entity.vo.TwilioPhoneVo;
import com.adbest.smsmarketingfront.service.MobileNumberService;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/phone")
public class MobileNumberController {

    @Autowired
    private MobileNumberService mobileNumberService;

    @RequestMapping("/view-list")
    public ReturnEntity list(){
        return ReturnEntity.success(mobileNumberService.findAll());
    }

    @RequestMapping("/open")
    public ReturnEntity init(){
        MobileNumberVo vo = mobileNumberService.init();
        return ReturnEntity.success(vo);
    }

    @RequestMapping("/search")
    public ReturnEntity search(SearchTwilioForm searchTwilioForm){
        List<TwilioPhoneVo> search = mobileNumberService.search(searchTwilioForm);
        return ReturnEntity.success(search);
    }

    @RequestMapping("/tollFreeSearch")
    public ReturnEntity tollFreeSearch(SearchTwilioForm searchTwilioForm){
        List<TwilioPhoneVo> search = mobileNumberService.tollFreeSearch(searchTwilioForm);
        return ReturnEntity.success(search);
    }

    @RequestMapping("/buy")
    public ReturnEntity buyPhone(String phoneNumber){
        boolean is = mobileNumberService.buyPhone(phoneNumber);
        return ReturnEntity.success(is);
    }
}
