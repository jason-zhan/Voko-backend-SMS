package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 用户套餐
 */
@Data
@Entity
public class CustomerMarketSetting implements Serializable {

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
     * @see Customer#id
     */
    @Column(nullable = false)
    private Long customerId;
    /**
     * 套餐id
     */
    private Long marketSettingId;
    /**
     * 订购时间
     */
    private Timestamp orderTime;

    /**
     * 过期时间
     */
    private Timestamp invalidTime;

    /**
     * 是否开启自动续费
     */
    private Boolean automaticRenewal;

    /**
     * 是否已过期（true:已过期）
     */
    private Boolean invalidStatus;

    public CustomerMarketSetting(MarketSetting marketSetting) {
        this.title = marketSetting.getTitle();
        this.smsTotal = marketSetting.getSmsTotal();
        this.mmsTotal = marketSetting.getMmsTotal();
        this.keywordTotal = marketSetting.getKeywordTotal();
        this.marketSettingId = marketSetting.getId();
    }

    public CustomerMarketSetting() {
    }
}
