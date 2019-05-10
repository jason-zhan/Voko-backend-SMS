package com.adbest.smsmarketingentity;

/**
 * 消息模板可用变量
 */
public enum MsgTemplateVariable {
    
    CUS_FIRSTNAME("#CUS_FIRSTNAME"),  // 用户名字
    CUS_LASTNAME("#CUS_LASTNAME"),  // 用户姓氏
    CON_FIRSTNAME("#CON_FIRSTNAME"),  // 联系人名字
    CON_LASTNAME("#CON_LASTNAME"),  // 联系人姓氏
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
