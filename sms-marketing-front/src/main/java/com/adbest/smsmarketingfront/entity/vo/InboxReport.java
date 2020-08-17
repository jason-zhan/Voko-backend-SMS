package com.adbest.smsmarketingfront.entity.vo;

public class InboxReport {
    private String sendDay;  // 发送时间
    private int count;
    public InboxReport(String sendDay, int count) {
        this.sendDay = sendDay;
        this.count = count;
    }
}
