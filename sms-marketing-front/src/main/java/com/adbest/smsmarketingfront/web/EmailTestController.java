package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingfront.util.TimeTools;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

// TODO 测试后删除
@Controller
@RequestMapping("/email")
public class EmailTestController {
    
    @RequestMapping("/page-test")
    public String pageTest(Model model) {
        model.addAttribute("monthAmount", 20);
        model.addAttribute("webLogin", "/login");
        return "./doc/email/monthly-financial-bill";
    }
}
