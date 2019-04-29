package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 联系人群组
 */
@Entity
@Data
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
    @Column(nullable = false)
    private  Boolean isDelete;  // 是否删除(true:已删除)
    
}
