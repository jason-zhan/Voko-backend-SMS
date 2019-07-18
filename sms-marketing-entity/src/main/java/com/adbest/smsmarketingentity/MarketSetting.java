package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 营销方案
 */
@Data
@Entity
@Table(name = "market_setting")
public class MarketSetting implements Serializable {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;  // 名称
    @Column(nullable = false)
    private Integer smsTotal;  // 短信条数
    @Column(nullable = false)
    private Integer mmsTotal;  // 彩信条数
    @Column(nullable = false)
    private Integer keywordTotal;  // 赠送关键字个数
    /**
     * 天数
     */
    @Column(nullable = false)
    private Integer daysNumber;
    /**
     * 价格
     */
    @Column(nullable = false)
    private BigDecimal price;
    /**
     * 套餐外短信单价
     */
    @Column(nullable = false)
    private BigDecimal smsPrice;
    /**
     * 套餐外彩信单价
     */
    @Column(nullable = false)
    private BigDecimal mmsPrice;
}
