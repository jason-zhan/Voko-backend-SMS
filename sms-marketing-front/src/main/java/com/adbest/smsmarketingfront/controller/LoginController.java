package com.adbest.smsmarketingfront.controller;

import com.adbest.smsmarketingfront.entity.form.CustomerForm;
import com.adbest.smsmarketingfront.service.CustomerService;
import com.adbest.smsmarketingfront.util.ResponseCode;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

@RestController
public class LoginController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ReturnMsgUtil returnMsgUtil;

    @RequestMapping("/nologin")
    public ReturnEntity login() {
        return ReturnEntity.fail(ResponseCode.T401.getStauts(),returnMsgUtil.msg("T401"));
    }

    @PostMapping("/register")
    public ReturnEntity register(CustomerForm vo) {
        boolean is = customerService.register(vo);
        return ReturnEntity.success(is);
    }
}
