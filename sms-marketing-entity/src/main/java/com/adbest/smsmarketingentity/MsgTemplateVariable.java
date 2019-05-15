package com.adbest.smsmarketingentity;

import java.util.HashSet;
import java.util.Set;

/**
 * 消息模板可用变量
 */
public enum MsgTemplateVariable {
    
    CUS_FIRSTNAME("#MY_FIRSTNAME"),  // 用户名字
    CUS_LASTNAME("#MY_LASTNAME"),  // 用户姓氏
    CON_FIRSTNAME("#CONTACTS_FIRSTNAME"),  // 联系人名字
    CON_LASTNAME("#CONTACTS_LASTNAME"),  // 联系人姓氏
    ;
    
    private String title;
    
    public static Set<String> valueSet(){
        Set<String> set = new HashSet<>();
        for (MsgTemplateVariable variable : MsgTemplateVariable.values()) {
            set.add(variable.getTitle());
        }
        return set;
    }
    
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
