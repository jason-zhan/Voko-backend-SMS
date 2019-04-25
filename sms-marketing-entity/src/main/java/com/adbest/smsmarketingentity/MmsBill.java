package com.adbest.smsmarketingentity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 彩信消费账单
 */
@Entity
@Data
public class MmsBill implements Serializable {
    
    @Id
    @GeneratedValue
    private Long id;
    /**
     * @see Customer#id
     */
    @Column(nullable = false)
    private Long customerId;
    @Column(nullable = false)
    private String describe;  // 描述
    @Column(nullable = false)
    private Integer amount;  // 彩信数量
    @Column(nullable = false)
    @CreationTimestamp
    private Timestamp time;  // 产生时间
}
