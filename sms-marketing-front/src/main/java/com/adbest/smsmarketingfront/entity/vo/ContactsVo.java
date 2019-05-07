package com.adbest.smsmarketingfront.entity.vo;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingentity.Customer;
import lombok.Data;

import javax.persistence.Column;
import java.sql.Timestamp;

@Data
public class ContactsVo {
    private Long id;  // 主键
    private String phone; // 号码
    private String firstName;  // 名字
    private String lastName;  // 姓氏
    private String email;  // 邮箱
    private String notes;  // 备注
    @Column(nullable = false)
    private Boolean inLock;  // 锁定(true:是)
    private Timestamp createTime;  // 创建时间
    public ContactsVo(Contacts contacts){
        this.id = contacts.getId();
        this.phone = contacts.getPhone();
        this.firstName = contacts.getFirstName();
        this.lastName = contacts.getLastName();
        this.email = contacts.getEmail();
        this.notes = contacts.getNotes();
        this.inLock = contacts.getInLock();
        this.createTime = contacts.getCreateTime();
    }

    public ContactsVo() {
    }
}