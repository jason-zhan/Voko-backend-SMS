package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
//@Table(name = "vkCDR_Accounts")
//@Entity
public class VkCDRAccounts {
    @Id
    private Integer id;
    private Integer i_env;
    private String h323_conf_id;
    private String CLI;
    private String CLD;
    private Integer setup_time;
    private Timestamp connect_time;
    private Timestamp disconnect_time;
    private Integer disconnect_cause;
    private Integer voice_quality;
    private String account_id;
    private Integer i_dest;
    private Integer charged_time;
    private BigDecimal charged_amount;
    private String history;
    private Integer i_vendor;
    private BigDecimal cost;
    private Integer i_customer;
    private String call_id;
    private Integer i_account;
    private Integer i_tariff;
    private Integer i_rate;
    private Integer i_service;
    private Timestamp bill_time;
    private Integer charged_quantity;
    private Integer used_quantity;
    private Integer bit_flags;
    private String h323_incoming_conf_id;
    private String rating_pattern;
    private byte[] originating_ip;
    private Integer split_order;
    private Integer billing_model;
    private Integer peak_level;
    private String subscriber_ip;
    private Integer i_dest_group;
    private Integer i_accessibility;
    private Integer i_invoice;
    /**
     * 是否已发送短信通知
     */
    private Integer sendStatus;
    /**
     * 是否已导入到系统
     */
    private Boolean inLeadin;
}
