package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingfront.service.EmailComponent;
import com.adbest.smsmarketingfront.util.EmailTools;
import com.adbest.smsmarketingfront.util.TimeTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
@Slf4j
public class EmailComponentImpl implements EmailComponent {
    
    @Autowired
    EmailTools emailTools;
    
    @Override
    public void sendPackageRemainingTip(String toAddress, int smsRemaining) {
        log.info("enter sendPackageRemainingTip, toAddress={}, smsRemaining={}", toAddress, smsRemaining);
        Assert.hasText(toAddress, "toAddress is empty");
        Map<String, Object> map = new HashMap<>();
        map.put("smsRemaining", smsRemaining);
        map.put("time",TimeTools.now() );
        emailTools.send("Package Remaining Tip", toAddress, "./doc/email/package-remaining-tip", map);
        log.info("leave sendPackageRemainingTip");
    }
    
    @Override
    public void sendMonthlyBill(Long customerId) {
        log.info("enter sendMonthlyBill, customerId={}", customerId);
        Assert.notNull(customerId, "customerId is empty");
        
        
        log.info("leave sendMonthlyBill");
    }
}
