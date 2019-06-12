package com.adbest.smsmarketingfront.entity.form;

import com.adbest.smsmarketingfront.util.PageBase;
import lombok.Data;

@Data
public class SelectContactsForm extends PageBase{

    private String keyWord;

    private String source;

    private String groupId;

    private Boolean inLock;
    
    @Override
    public String toString() {
        return "SelectContactsForm{" +
                "keyWord='" + keyWord +
                ", source='" + source +
                ", groupId='" + groupId +
                ", inLock=" + inLock +
                ", page=" + page +
                ", size=" + size +
                "}";
    }
}
