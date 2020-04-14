package com.adbest.smsmarketingfront;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.service.MarketSettingService;
import com.adbest.smsmarketingfront.util.CommonMessage;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

@Configuration
@EntityScan(basePackages = "com.adbest.smsmarketingentity")
@ServletComponentScan(basePackages = "com.adbest.smsmarketingfront.handler")
public class SpringInitializer implements InitializingBean {

    @Autowired
    private Environment environment;

    @Autowired
    private MarketSettingService marketSettingService;

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
    
    @Bean
    public ResourceBundle resourceBundle() {
//        return ResourceBundle.getBundle("lang", Locale.CHINA);
        return ResourceBundle.getBundle("lang", Locale.US);
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 使用Jackson2JsonRedisSerialize 替换默认序列化
//        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        // 设置value的序列化规则和 key的序列化规则
        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
    
    @Bean
    public Map<Integer, String> messagePlanStatusMap(ResourceBundle bundle) {
        return getBundleValuesMap(MessagePlanStatus.class, "message-plan-status-", bundle);
    }
    
    @Bean
    public Map<Integer, String> inboxStatusMap(ResourceBundle bundle) {
        return getBundleValuesMap(InboxStatus.class, "inbox-status-", bundle);
    }
    
    @Bean
    public Map<Integer, String> outboxStatusMap(ResourceBundle bundle) {
        return getBundleValuesMap(OutboxStatus.class, "outbox-status-", bundle);
    }
    
    @Bean
    public Map<Integer, String> systemMsgTemplateTypeMap(ResourceBundle bundle) {
        return getBundleValuesMap(SystemMsgTemplateType.class, "sys-msg-template-type-", bundle);
    }
    
    @Bean
    public Set<String> msgTemplateVariableSet() {
        return MsgTemplateVariable.valueSet();
    }
    
    
    @Override
    public void afterPropertiesSet() throws Exception {
        initMarketSetting();
    
    }
    
    private <T extends Enum> Map<Integer, String> getBundleValuesMap(Class<T> tClass, String prefix, ResourceBundle bundle) {
        Map<Integer, String> map = new HashMap<>();
        T[] ts = tClass.getEnumConstants();
        try {
            for (T t : ts) {
                Integer value = (Integer) tClass.getMethod("getValue").invoke(t);
                String text = bundle.getString(prefix + value);
                Assert.hasText(text, CommonMessage.OBJECT_NOT_FOUND + ": " + prefix);
                map.put(value, text);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("getBundleValuesMap execute err: ", e);
        }
        return map;
    }
    
    private <T extends Enum> Map<Integer, String> getValuesMap(Class<T> tClass) {
        Map<Integer, String> map = new HashMap<>();
        T[] ts = tClass.getEnumConstants();
        try {
            for (T t : ts) {
                map.put((Integer) tClass.getMethod("getValue").invoke(t),
                        (String) tClass.getMethod("getTitle").invoke(t));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("getValuesMap execute err: ", e);
        }
        return map;
    }

    private void initMarketSetting(){
        Long row = marketSettingService.count();
        if (row==0){
            MarketSetting marketSetting = new MarketSetting();
            marketSetting.setDaysNumber(Integer.valueOf(environment.getProperty("marketSetting.daysNumber")));
            marketSetting.setKeywordTotal(Integer.valueOf(environment.getProperty("marketSetting.keywordTotal")));
            marketSetting.setTitle(environment.getProperty("marketSetting.title"));
            marketSetting.setSmsTotal(Integer.valueOf(environment.getProperty("marketSetting.smsTotal")));
            marketSetting.setPrice(BigDecimal.valueOf(0));
            marketSetting.setMmsTotal(Integer.valueOf(environment.getProperty("marketSetting.mmsTotal")));
            marketSetting.setMmsPrice(BigDecimal.valueOf(0));
            marketSetting.setSmsPrice(BigDecimal.valueOf(0));
            marketSettingService.save(marketSetting);
        }
    }
}
