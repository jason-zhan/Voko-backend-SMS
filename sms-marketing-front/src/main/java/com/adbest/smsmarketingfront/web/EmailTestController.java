package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingfront.util.TimeTools;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/email")
public class EmailTestController {
    
    @RequestMapping("/page-test")
    public String pageTest(Model model) {
        model.addAttribute("date", TimeTools.now());
        model.addAttribute("smsTotal", 200);
//        model.addAttribute("remaining", 100);
        List<String> billList = new ArrayList<>();
        billList.add("2019-5-3 17:06:16 sent sms 198");
        billList.add("2019-5-20 17:06:16 sent sms 200");
        billList.add("2019-5-31 17:06:16 sent sms 205");
        model.addAttribute("billList", billList);
        return "./doc/email/monthly-bill";
    }
}
