package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 手机号码库
 */
@Entity
@Data
public class MbNumberLib implements Serializable {
    
    @Id
    @GeneratedValue
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
    private Timestamp createTime;  // 创建时间
}
