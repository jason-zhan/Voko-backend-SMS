package com.adbest.smsmarketingentity;

/**
 * 消息状态返回码 - from twilio
 * [api]https://www.twilio.com/docs/sms/tutorials/how-to-confirm-delivery-java
 * [api]https://www.twilio.com/docs/sms/api/message-resource#message-status-values
 * @see MessageRecord#returnCode
 */
public enum MessageReturnCode {
    
    QUEUED(1,"QUEUED"),  // 队列中
    FAILED(2,"FAILED"),  // 发送失败 (如 设备不支持接收彩信 - twilio不收费)
    SENT(3,"SENT"),  // 已发送 (twilio已发送给最近的运营商)
    DELIVERED(4,"DELIVERED"),  // 已送达 (已到达目标设备)
    UNDELIVERED(5,"UNDELIVERED"),  // 未送达 (如 被运营商内容过滤)
    ;
    
    private int value;
    private String title;
    
    MessageReturnCode(int value, String title) {
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
