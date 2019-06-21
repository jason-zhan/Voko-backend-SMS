package com.adbest.smsmarketingfront.util;

/**
 * 计数器
 * 非线程安全
 */
public class Counter {
    
    private int value;
    
    
    public int increase() {
        return ++value;
    }
    
    public int decrease(){
        return --value;
    }
    
    
    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value = value;
    }
}
