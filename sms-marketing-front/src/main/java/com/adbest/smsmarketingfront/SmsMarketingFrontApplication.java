package com.adbest.smsmarketingfront;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
public class SmsMarketingFrontApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmsMarketingFrontApplication.class, args);
    }

}
