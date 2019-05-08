package com.adbest.smsmarketingentity;

/**
 * 消息状态 [本系统自定义]
 */
public enum MessageStatus {
    
    draft(0, "DRAFT"),  // 草稿
    planning(1, "PLANNING"),  // 计划中
    queue(2, "QUEUE"),  // 队列中
    sent(3, "SENT"),  // 已发送
    DELIVERED(4, "DELIVERED"),  // 已送达
    UNDELIVERED(5, "UNDELIVERED"),  // 未送达
    FAILED(6, "FAILED"),  // 发送失败
    ;
    
    private int value;
    private String title;
    
    MessageStatus(int value, String title) {
        this.value = value;
        this.title = title;
    }
    
    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value = value;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
}
