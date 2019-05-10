package com.adbest.smsmarketingentity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 手机号码库
 */
@Entity
@Data
public class MobileNumber implements Serializable {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    /**
     * 用户id
     *
     * @see Customer#id
     */
    private Long customerId;
    @Column(nullable = false, unique = true)
    private String number;  // 手机号码
    /**
     * 城市id
     *
     * @see UsArea#id
     */
    private Long cityId;
    private Boolean disable;  // 是否可用(true:禁用)
    @CreationTimestamp
    private Timestamp createTime;  // 创建时间
}
