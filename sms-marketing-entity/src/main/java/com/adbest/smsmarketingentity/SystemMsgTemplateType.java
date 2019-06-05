package com.adbest.smsmarketingentity;

public enum  SystemMsgTemplateType {
    
    FITNESS(1, "FITNESS"),  // 健身
    RELIGIOUS_GROUPS(2, "RELIGIOUS_GROUPS"),  // 宗教团体
    RESTAURANTS(3, "RESTAURANTS"),  // 餐饮
    BAR_OR_NIGHTLIFE(4, "BAR_OR_NIGHTLIFE"),  // 酒吧/夜生活
    REAL_ESTATE(5, "REAL_ESTATE"),  // 房地产
    NONPROFITS(6, "NONPROFITS"),  // 非盈利组织
    RETAIL(7, "RETAIL"),  // 零售
    SPORTS_CLUBS(8, "SPORTS_CLUBS"),  // 体育俱乐部
    HOLIDAY(9, "HOLIDAY"),  // 度假
    ;
    
    private int value;
    private String title;
    
    SystemMsgTemplateType(int value, String title) {
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
