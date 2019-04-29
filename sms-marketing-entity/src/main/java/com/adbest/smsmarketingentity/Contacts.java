package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 联系人 [客户手动导入的联系人]
 */
@Entity
@Data
public class Contacts implements Serializable {
    
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
    private String phone; // 号码
    private String customerName;  // 姓名
    private String email;  // 邮箱
    @Column(nullable = false)
    private Boolean inLock;  // 锁定(true:是)
    @Column(nullable = false)
    private  Boolean isDelete;  // 是否删除(true:已删除)
    private Timestamp createTime;  // 创建时间
    
}
