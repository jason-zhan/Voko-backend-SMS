package com.adbest.smsmarketingfront.entity.vo;

import com.adbest.smsmarketingentity.Customer;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

@Data
public class UserDetailsVo implements UserDetails {
    private Long id;  // 主键
    private String email;  // 邮箱 (作为用户名)
    private String firstName;  // 名字
    private String lastName;  // 姓氏
    private String customerName;
    private String industry;  // 行业
    private String organization;  // 单位（公司/机构）
    private Timestamp registerTime;  // 注册时间
    private Boolean disable;  // 注册时间

    public UserDetailsVo() {
    }

    public UserDetailsVo(Customer customer) {
        this.id = customer.getId();
        this.email = customer.getEmail();
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
        this.customerName = customer.getCustomerName();
        this.industry = customer.getIndustry();
        this.organization = customer.getOrganization();
        this.registerTime = customer.getRegisterTime();
        this.disable = customer.getDisable();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<GrantedAuthority>();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !disable;
    }
}
