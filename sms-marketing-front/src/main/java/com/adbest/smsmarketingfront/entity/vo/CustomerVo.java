package com.adbest.smsmarketingfront.entity.vo;

import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingentity.UsArea;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class CustomerVo implements Serializable {
    private Long id;  // 主键
    private String email;  // 邮箱 (作为用户名)
    private String firstName;  // 名字
    private String lastName;  // 姓氏
    private String customerName;
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
        this.customerName = customer.getCustomerName();
        this.industry = customer.getIndustry();
        this.organization = customer.getOrganization();
        this.registerTime = customer.getRegisterTime();
    }
}
