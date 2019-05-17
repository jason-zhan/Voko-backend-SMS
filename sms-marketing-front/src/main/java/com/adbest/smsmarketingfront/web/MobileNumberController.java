package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingentity.MobileNumber;
import com.adbest.smsmarketingfront.entity.vo.MobileNumberVo;
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

}
