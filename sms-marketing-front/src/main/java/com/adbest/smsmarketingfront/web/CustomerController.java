package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingfront.entity.form.CustomerForm;
import com.adbest.smsmarketingfront.service.CustomerService;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

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

    @RequestMapping("/getCode")
    public ReturnEntity getCode(String email, HttpServletRequest request){
        boolean is = customerService.getCode(email, request);
        return ReturnEntity.success(is);
    }

    @RequestMapping("/password")
    public ReturnEntity password(String code,String password, HttpServletRequest request){
        boolean is = customerService.updatePasswordByCode(code, password, request);
        return ReturnEntity.success(is);
    }

    @RequestMapping("/git")
    public ReturnEntity git(){
        try {
            String cmd = "sh test.sh ";
            File dir = null;
            dir = new File("/opt/");
            String[] evnp = {"val=2", "call=Bash Shell"};
            Process process = Runtime.getRuntime().exec(cmd, evnp, dir);
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            input.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return ReturnEntity.success("success");
    }
}
