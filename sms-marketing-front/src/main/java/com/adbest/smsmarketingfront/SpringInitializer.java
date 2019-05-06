package com.adbest.smsmarketingfront;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import java.util.Locale;
import java.util.ResourceBundle;

@Configuration
@EntityScan(basePackages = "com.adbest.smsmarketingentity")
@ServletComponentScan(basePackages = "com.adbest.smsmarketingfront.handler")
public class SpringInitializer implements InitializingBean {
    
    
    @Bean
    @Autowired
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
    
    @Bean
    public ResourceBundle resourceBundle(){
        return ResourceBundle.getBundle("lang",Locale.CHINA);
    }
    
    
    @Override
    public void afterPropertiesSet() throws Exception {
    
    }
}
