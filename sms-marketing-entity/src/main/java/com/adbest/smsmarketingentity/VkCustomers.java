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
@Entity
@Table(name = "vkcustomers")
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

    @Transient
    private String password = "123";
    /**
     * 是否已导入到系统
     */
    private Boolean inLeadin;

    public VkCustomers(Boolean inLeadin, String email,Integer i_customer,String firstname,String lastname,String login,String name,String phone1,String phone2) {
        this.i_customer = i_customer;
        this.name = name;
        this.balance = balance;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.cont1 = cont1;
        this.phone1 = phone1;
        this.cont2 = cont2;
        this.phone2 = phone2;
        this.login = login;
        this.i_distributor = i_distributor;
        this.baddr1 = baddr1;
        this.baddr2 = baddr2;
        this.baddr3 = baddr3;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
        this.tax_id = tax_id;
        this.note = note;
        this.creation_date = creation_date;
        this.i_rep = i_rep;
        this.due_date = due_date;
        this.billed_to = billed_to;
        this.inLeadin = inLeadin;
    }
}