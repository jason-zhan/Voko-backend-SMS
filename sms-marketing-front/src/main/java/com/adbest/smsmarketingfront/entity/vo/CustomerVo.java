package com.adbest.smsmarketingfront.entity.vo;

import com.adbest.smsmarketingentity.Customer;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class CustomerVo implements Serializable {
    private Long id;  // 主键
    private String email;  // 邮箱 (作为用户名)
    private String firstName;  // 名字
    private String lastName;  // 姓氏
    private String industry;  // 行业
    private String organization;  // 单位（公司/机构）
    private Timestamp registerTime;  // 注册时间

    public CustomerVo() {
    }

    public CustomerVo(Customer customer) {
        this.id = customer.getId();
        this.email = customer.getEmail();
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
        this.industry = customer.getIndustry();
        this.organization = customer.getOrganization();
        this.registerTime = customer.getRegisterTime();
    }

    public CustomerVo(UserDetailsVo customer) {
        this.id = customer.getId();
        this.email = customer.getEmail();
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
        this.industry = customer.getIndustry();
        this.organization = customer.getOrganization();
        this.registerTime = customer.getRegisterTime();
    }
}
