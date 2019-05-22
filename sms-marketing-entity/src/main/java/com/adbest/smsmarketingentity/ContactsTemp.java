package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "contacts_temp")
public class ContactsTemp implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;  // 主键
    private Long customerId;
    private String phone; // 号码
    private String firstName;  // 名字
    private String lastName;  // 姓氏
    private String email;  // 邮箱
    private String notes;  // 备注
    private String tempSign;  // 记号
}
