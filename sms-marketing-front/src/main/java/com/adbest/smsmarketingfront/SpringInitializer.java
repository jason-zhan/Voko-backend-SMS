package com.adbest.smsmarketingfront;

import com.adbest.smsmarketingentity.InboxStatus;
import com.adbest.smsmarketingentity.MessagePlanStatus;
import com.adbest.smsmarketingentity.MsgTemplateVariable;
import com.adbest.smsmarketingentity.OutboxStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.persistence.EntityManager;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
    public ResourceBundle resourceBundle() {
//        return ResourceBundle.getBundle("lang", Locale.CHINA);
        return ResourceBundle.getBundle("lang", Locale.US);
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
    public Map<Integer, String> messagePlanStatusMap() {
        return getValuesMap(MessagePlanStatus.class);
    }
    
    @Bean
    public Map<Integer, String> inboxStatusMap() {
        return getValuesMap(InboxStatus.class);
    }
    
    @Bean
    public Map<Integer, String> outboxStatusMap() {
        return getValuesMap(OutboxStatus.class);
    }
    
    @Bean
    public Set<String> msgTemplateVariableSet() {
        return MsgTemplateVariable.valueSet();
    }
    
    
    @Bean
    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
    
    
    }
    
    
    private <T extends Enum> Map<Integer, String> getValuesMap(Class<T> tClass) {
        Map<Integer, String> map = new HashMap<>();
        T[] ts = tClass.getEnumConstants();
        try {
            for (T t : ts) {
                map.put((Integer) tClass.getMethod("getValue").invoke(t),
                        (String) tClass.getMethod("getTitle").invoke(t));
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            return map;
        }
    }
}
