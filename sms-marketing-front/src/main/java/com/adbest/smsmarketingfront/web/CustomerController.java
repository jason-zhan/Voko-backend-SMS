package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingfront.entity.form.CustomerForm;
import com.adbest.smsmarketingfront.service.CustomerService;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        boolean is = customerService.updateInfo(customerForm);
        return ReturnEntity.success(is);
    }
}
