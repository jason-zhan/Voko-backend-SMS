package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 联系人群组
 */
@Entity
@Data
@Table(name="contacts_group",uniqueConstraints = {@UniqueConstraint(columnNames={"customerId", "title"})})
public class ContactsGroup implements Serializable {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;  // 主键
    /**
     * 客户id
     * @see Customer#id
     */
    @Column(nullable = false)
    private Long customerId;
    @Column(nullable = false)
    private String title;  // 名称
    private String description;  // 描述

}
