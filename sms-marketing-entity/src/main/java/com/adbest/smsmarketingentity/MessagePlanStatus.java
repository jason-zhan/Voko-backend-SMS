package com.adbest.smsmarketingentity;

/**
 * 消息发送任务状态
 * @see MessagePlan#status
 */
public enum MessagePlanStatus {
    
    EDITING(0, "EDITING"),  // 编辑中
    SCHEDULING(1, "SCHEDULING"),  // 计划中
    QUEUING(2, "QUEUING"),  // 队列中
    EXECUTING(3, "EXECUTING"),  // 执行中
    EXECUTION_COMPLETED(4, "EXECUTION_COMPLETED"),  // 执行完毕
    FINISHED(5, "FINISHED"),  // 已完成(全部执行完成，不再修改)
    INSUFFICIENT_BALANCE(6, "INSUFFICIENT_BALANCE"),  // 余量不足
    ;
    
    private int value;
    private String title;
    
    MessagePlanStatus(int value, String title) {
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
