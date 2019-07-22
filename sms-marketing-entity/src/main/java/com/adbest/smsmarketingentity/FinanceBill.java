package com.adbest.smsmarketingentity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 客户金融消费记录
 */
@Data
@Entity
@Table(name = "finance_bill")
public class FinanceBill implements Serializable {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    /**
     * @see Customer#id
     */
    @Column(nullable = false)
    private Long customerId;
    @Column(nullable = false)
    private String infoDescribe;  // 描述
    @Column(columnDefinition = "decimal(10,3)",nullable = false)
    private BigDecimal amount;  // 金额
    @Column(nullable = false)
    @CreationTimestamp
    private Timestamp time;  // 产生时间
    /**
     * 账单状态
     * @see FinanceBillStatus
     */
    @Column(nullable = false)
    private Integer status;
}
