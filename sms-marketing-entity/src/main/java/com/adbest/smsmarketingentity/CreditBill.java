package com.adbest.smsmarketingentity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 用户信用账单
 */
@Entity
@Data
public class CreditBill implements Serializable {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;  // 主键
    @Column(nullable = false)
    private Long customerId;  // 用户id
    @Column(nullable = false)
    @CreationTimestamp
    private Timestamp time;  // 产生时间
    @Column(nullable = false)
    private BigDecimal amount;  // 金额
    @Column(nullable = false)
    private String remark;  // 备注
    
    public CreditBill() {
    }
    
    public CreditBill(Long customerId, BigDecimal amount, String remark) {
        this.customerId = customerId;
        this.amount = amount;
        this.remark = remark;
    }
}
