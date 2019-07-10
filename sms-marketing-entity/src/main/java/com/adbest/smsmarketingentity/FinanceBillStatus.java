package com.adbest.smsmarketingentity;

/**
 * 金融账单状态
 * @see FinanceBill
 */
public enum FinanceBillStatus {
    
    UNSUBMITTED(0,"UNSUBMITTED"),  // 未提交
    SUBMITTED(1,"SUBMITTED"),  // 已提交
    ;
    
    private int value;
    private String title;
    
    FinanceBillStatus(int value, String title) {
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
