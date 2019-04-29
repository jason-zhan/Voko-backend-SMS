package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 营销方案
 */
@Data
@Entity
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
}
