package com.adbest.smsmarketingentity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 营销方案
 */
public class MarketSetting implements Serializable {
    
    @Id
    @GeneratedValue
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
