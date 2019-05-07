package com.adbest.smsmarketingentity;

/**
 * 消息模板可用变量
 */
public enum MsgTemplateVariable {
    
    CUS_FIRSTNAME("#CUS_FIRSTNAME"),
    CUS_LASTNAME("#CUS_LASTNAME"),
    CON_FIRSTNAME("#CON_FIRSTNAME"),
    CON_LASTNAME("#CON_LASTNAME"),
    ;
    
    private String title;
    
    MsgTemplateVariable(String title) {
        this.title = title;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
}
