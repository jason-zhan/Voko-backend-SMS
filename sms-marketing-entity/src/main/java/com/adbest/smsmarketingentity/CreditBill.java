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
 * 信用消费账单
 */
@Entity
@Data
public class CreditBill implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long customerId;  // 用户id
    /**
     * @see CreditBillType
     */
    @Column(nullable = false)
    private Integer type;  // 类型
    @Column(nullable = false)
    private Long referId;  // 关联的实体id
    @Column(columnDefinition = "decimal(10,3)",nullable = false)
    private BigDecimal amount;  // 金额
    @Column(nullable = false)
    private String remark;  // 备注
    @CreationTimestamp
    @Column(nullable = false)
    private Timestamp time;  // 创建时间
}
