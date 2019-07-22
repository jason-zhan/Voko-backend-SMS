package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * V公司商户表
 */
@Data
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
    private String password;
    /**
     * 是否已导入到系统
     */
    private Boolean inLeadin;

    public VkCustomers(Boolean inLeadin, String email,Integer i_customer,String firstname,String lastname,String login,String name,String phone1,String phone2,String password) {
        this.i_customer = i_customer;
        this.name = name;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.login = login;
        this.inLeadin = inLeadin;
        this.password = password;
    }

    public VkCustomers() {
    }
}