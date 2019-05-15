package com.adbest.smsmarketingentity;

/**
 * 消息状态 [本系统自定义]
 * @see MessageRecord#status
 */
public enum  InboxStatus {
    
    UNREAD(0, "UNREAD"),  // 未读
    ALREADY_READ(1, "ALREADY_READ"),  // 已读
    
    ;
    
    
    private int value;
    private String title;
    
    InboxStatus(int value, String title) {
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
