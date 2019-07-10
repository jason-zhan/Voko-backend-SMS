package com.adbest.smsmarketingfront.entity.enums;

import java.util.concurrent.TimeUnit;

/**
 * redis key 管理
 * 当 {@link #expireTime} 与 {@link #timeUnit} 为空时表示永不失效
 */
public enum RedisKey {
    
    TMP_PLAN_UNIQUE_CONTACTS("tmp:createMsgPlan:", 1L, TimeUnit.HOURS),  // 消息发送任务-联系人唯一验证
    INIT_PHONE("init:phone:", 10l, TimeUnit.SECONDS),  //初始化用户手机号
    BUY_KEYWORD("buy:keyword", 5l, TimeUnit.SECONDS),  //关键字购买

    ;
    
    private String key;
    private Long expireTime;
    private TimeUnit timeUnit;
    
    RedisKey(String key, Long expireTime, TimeUnit timeUnit) {
        this.key = key;
        this.expireTime = expireTime;
        this.timeUnit = timeUnit;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public Long getExpireTime() {
        return expireTime;
    }
    
    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }
    
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
    
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
}
