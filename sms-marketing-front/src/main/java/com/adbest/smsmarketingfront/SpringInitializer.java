package com.adbest.smsmarketingfront;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan(basePackages = "com.adbest.smsmarketingentity")
public class SpringInitializer implements InitializingBean {
    
    
    
    
    
    
    
    @Override
    public void afterPropertiesSet() throws Exception {
    
    }
}
