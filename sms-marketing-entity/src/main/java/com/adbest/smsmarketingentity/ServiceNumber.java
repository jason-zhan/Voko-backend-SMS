package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 服务短号库
 */
@Entity
@Data
@Table(name = "service_number")
public class ServiceNumber implements Serializable {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String number;  // 号码
    private Boolean disable;  // 是否可用(true:禁用)
    private Timestamp createTime;  // 创建时间
}
