package com.adbest.smsmarketingentity;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 客户金融消费记录
 */
public class FinanceBill implements Serializable {
    
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
    private BigDecimal amount;  // 金额
    @Column(nullable = false)
    @CreationTimestamp
    private Timestamp time;  // 产生时间
}
