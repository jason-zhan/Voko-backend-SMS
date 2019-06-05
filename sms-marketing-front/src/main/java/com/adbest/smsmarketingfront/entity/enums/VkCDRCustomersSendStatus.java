package com.adbest.smsmarketingfront.entity.enums;

public enum VkCDRCustomersSendStatus {

    UNWANTED_SENT(1, "不需要发送"),
    ALREADY_SENT(2, "已发送"),
    ;
    private int value;
    private String title;
    VkCDRCustomersSendStatus(int value, String title) {
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
