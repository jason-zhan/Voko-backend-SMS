package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingfront.entity.form.CustomerForm;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import com.adbest.smsmarketingfront.service.CustomerService;
import com.adbest.smsmarketingfront.util.ResponseCode;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;

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

//    @PostMapping("/register")
    public ReturnEntity register(CustomerForm vo, HttpServletRequest request) {
        CustomerVo customerVo = customerService.register(vo, request);
        return ReturnEntity.success(customerVo);
    }

    @RequestMapping(value="/verifyCode")
    @ResponseBody
    public ReturnEntity getMiaoshaVerifyCod(HttpServletRequest request, HttpServletResponse response) {
        try {
            BufferedImage image  = customerService.createVerifyCode(request);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        }catch(Exception e) {
            e.printStackTrace();
            return ReturnEntity.fail(returnMsgUtil.msg("T500"));
        }
    }
}
