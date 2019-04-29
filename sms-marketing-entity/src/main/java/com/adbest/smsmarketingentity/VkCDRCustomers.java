package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Table(name = "vkCDR_Customers")
@Entity
public class VkCDRCustomers implements Serializable {
    @Id
    private String id;
    private String i_env;
    private String h323_conf_id;
    /**
     * 用户电话号码
     */
    private String CLI;
    private String CLD;
    private String setup_time;
    private String connect_time;
    private String disconnect_time;
    private String disconnect_cause;
    private String voice_quality;
    private String i_customer;
    private String i_dest;
    private String charged_time;
    private String charged_amount;
    private String history;
    private String i_vendor;
    private String cost;
    private String call_id;
    private String i_tariff;
    private String i_rate;
    private String i_service;
    private String bill_time;
    private String charged_quantity;
    private String used_quantity;
    private String bit_flags;
    private String h323_incoming_conf_id;
    private String rating_pattern;
    private String originating_ip;
    private String split_order;
    private String peak_level;
    private String i_dest_group;
    private String i_accessibility;
    private String i_invoice;
    /**
     * 是否已发送短信通知
     */
    private Boolean inSend;
}
