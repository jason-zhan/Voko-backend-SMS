package com.adbest.smsmarketingentity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 短信消费账单
 */
@Entity
@Data
public class SmsBill implements Serializable {
    
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
    @Column(nullable = false)
    private Integer amount;  // 短信数量
    @Column(nullable = false)
    @CreationTimestamp
    private Timestamp time;  // 产生时间

    public SmsBill(Long customerId, String infoDescribe, Integer amount) {
        this.customerId = customerId;
        this.infoDescribe = infoDescribe;
        this.amount = amount;
    }

    public SmsBill() {
    }
}
