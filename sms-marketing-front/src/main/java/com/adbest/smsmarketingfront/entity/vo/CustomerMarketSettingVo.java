package com.adbest.smsmarketingfront.entity.vo;

import com.adbest.smsmarketingentity.CustomerMarketSetting;
import com.adbest.smsmarketingfront.util.TimeTools;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class CustomerMarketSettingVo implements Serializable {
    private String title;  // 名称
    private Integer smsTotal;  // 短信条数
    private Integer mmsTotal;  // 彩信条数
    private Integer keywordTotal;  // 赠送关键字个数

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
     * 剩余天数
     */
    private Integer daysRemaining;

    public CustomerMarketSettingVo(CustomerMarketSetting customerMarketSetting) {
        this.title = customerMarketSetting.getTitle();
        this.smsTotal = customerMarketSetting.getSmsTotal();
        this.mmsTotal = customerMarketSetting.getMmsTotal();
        this.keywordTotal = customerMarketSetting.getKeywordTotal();
        this.marketSettingId = customerMarketSetting.getMarketSettingId();
        this.orderTime = customerMarketSetting.getOrderTime();
        this.invalidTime = customerMarketSetting.getInvalidTime();
        this.daysRemaining = TimeTools.getDiffDays(TimeTools.now(), customerMarketSetting.getInvalidTime());
    }

    public CustomerMarketSettingVo() {
    }
}
