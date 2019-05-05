package com.adbest.smsmarketingfront.controller;

import com.adbest.smsmarketingfront.entity.vo.CustomerForm;
import com.adbest.smsmarketingfront.service.CustomerService;
import com.adbest.smsmarketingfront.util.ResponseCode;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @Autowired
    private CustomerService customerService;

    @RequestMapping("/nologin")
    public ReturnEntity login() {
        return ReturnEntity.fail(ResponseCode.T401);
    }

    @PostMapping("/register")
    public ReturnEntity register(CustomerForm vo) {
        boolean is = customerService.register(vo);
        return ReturnEntity.success(is);
    }
}
