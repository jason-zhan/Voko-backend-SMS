package com.adbest.smsmarketingfront;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SmsMarketingFrontApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmsMarketingFrontApplication.class, args);
    }

}
