package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingfront.entity.form.CustomerForm;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import com.adbest.smsmarketingfront.service.CustomerService;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @RequestMapping("/changePassword")
    public ReturnEntity changePassword(String password, String newPassword){
        boolean is = customerService.changePassword(password, newPassword);
        return ReturnEntity.success(is);
    }

    @RequestMapping("/updateInfo")
    public ReturnEntity updateInfo(CustomerForm customerForm){
        CustomerVo vo = customerService.updateInfo(customerForm);
        return ReturnEntity.success(vo);
    }

    @RequestMapping("/getCode")
    public ReturnEntity getCode(String username, HttpServletRequest request){
        String mag = customerService.getCode(username, request);
        return ReturnEntity.success(mag);
    }

    @RequestMapping("/password")
    public ReturnEntity password(String code,String password, HttpServletRequest request){
        boolean is = customerService.updatePasswordByCode(code, password, request);
        return ReturnEntity.success(is);
    }
}
