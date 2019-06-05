package com.adbest.smsmarketingfront.util;

import com.adbest.smsmarketingfront.util.msoffice.ExcelUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Locale;
import java.util.Map;

@Component
public class EmailTools {
    
    @Autowired
    JavaMailSender javaMailSender;
    @Autowired
    TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public void send(String subject, String toAddress, String templatePath, Map<String, Object> data) {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        String content = templateEngine.process(templatePath, new Context(Locale.US, data));
        try {
            helper.setFrom(fromEmail);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(content, true);
        } catch (MessagingException e) {
            throw new RuntimeException("set message attr err: ", e);
        }
        javaMailSender.send(message);
    }
    
    public void sendWithWorkbook(String subject, String toAddress, String templatePath, Map<String, Object> data, HSSFWorkbook workbook) {
        MimeMessage message = javaMailSender.createMimeMessage();
        String content = templateEngine.process(templatePath, new Context(Locale.US, data));
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(content, true);
            helper.addAttachment(workbook.getSheetName(0), new InputStreamResource(ExcelUtil.getInputStream(workbook)), "application/vnd.ms-excel");
        } catch (MessagingException e) {
            throw new RuntimeException("set MimeMessageHelper err: ", e);
        }
    }
}
