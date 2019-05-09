package com.adbest.smsmarketingentity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 服务短号库
 */
public class ServiceNumber implements Serializable {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String number;  // 号码
    private Boolean disable;  // 是否可用(true:禁用)
    private Timestamp createTime;  // 创建时间
}
