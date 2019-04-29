package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

/**
 * V公司商户表
 */
@Data
@Entity
@Table(name = "vkCustomers")
public class VkCustomers implements Serializable {
    @Id
    private Integer i_customer;
    private String name;
    private BigDecimal balance;
    private String firstname;
    private String lastname;
    private String email;
    private String cont1;
    private String phone1;
    private String cont2;
    private String phone2;
    private String login;
    private Timestamp i_distributor;
    private String baddr1;
    private String baddr2;
    private String baddr3;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String tax_id;
    private String note;
    private Timestamp creation_date;
    private Integer i_rep;
    private Date due_date;
    private Timestamp billed_to;
    /**
     * 是否已导入到系统
     */
    private Boolean inLeadin;
}