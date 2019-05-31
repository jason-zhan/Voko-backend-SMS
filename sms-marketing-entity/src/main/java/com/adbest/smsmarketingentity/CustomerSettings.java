package com.adbest.smsmarketingentity;

import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 个人设置
 */
@Data
@Entity
public class CustomerSettings implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 电话结束回复短信
     */
    @Column(nullable = false)
    private Boolean callReminder;

    /**
     * 回复内容
     */
    @Lob
    private String content;

    /**
     *  最近修改时间
     */
    @UpdateTimestamp
    private Timestamp updateTime;

    @Column(nullable = false, unique = true)
    private Long customerId;

    /**
     * 号码领取状态
     */
    private Boolean numberReceivingStatus;

    public CustomerSettings() {
    }

    public CustomerSettings(Boolean callReminder, Long customerId, Boolean numberReceivingStatus) {
        this.callReminder = callReminder;
        this.customerId = customerId;
        this.numberReceivingStatus = numberReceivingStatus;
    }
}
