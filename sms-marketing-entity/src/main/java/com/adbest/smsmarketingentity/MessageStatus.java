package com.adbest.smsmarketingentity;

/**
 * 消息状态 [本系统自定义]
 * @see MessageRecord#status
 */
public enum MessageStatus {
    
    // 发件状态
    DRAFT(0, "DRAFT"),  // 草稿
    PLANNING(1, "PLANNING"),  // 计划中
    QUEUE(2, "QUEUE"),  // 队列中
    SENT(3, "SENT"),  // 已发送
    DELIVERED(4, "DELIVERED"),  // 已送达
    UNDELIVERED(5, "UNDELIVERED"),  // 未送达
    FAILED(6, "FAILED"),  // 发送失败
    
    // 收件状态
    UNREAD(8, "UNREAD"),  // 未读
    ALREADY_READ(9, "ALREADY_READ"),  // 已读
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
