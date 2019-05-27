package com.adbest.smsmarketingfront.entity.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ContactsGroupVo implements Serializable {

    private Long id;

    private String title;  // 名称

    private String num; //联系人数量

    public ContactsGroupVo() {
    }

    public ContactsGroupVo(Long id, String title, String num) {
        this.id = id;
        this.title = title;
        this.num = num;
    }
}
