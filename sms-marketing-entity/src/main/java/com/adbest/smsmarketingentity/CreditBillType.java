package com.adbest.smsmarketingentity;

/**
 * 信用账单类型
 *
 * @see CreditBill
 */
public enum CreditBillType {
    
    ADJUST_MAX_CREDIT(0, "ADJUST_MAX_CREDIT"),  // 调整最大信用额度
    MESSAGE_PLAN(1, "MESSAGE_PLAN"),  // 任务消费
    KEYWORD(2, "KEYWORD"),  // 购买关键字
    CUSTOMER_MOBILE(3, "CUSTOMER_MOBILE"),  // 购买手机号
    RESUME_AVAILABLE_CREDIT(4, "RESUME_AVAILABLE_CREDIT"),  // 恢复可用信用额度
    ;
    
    
    private int value;
    private String title;
    
    CreditBillType(int value, String title) {
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
