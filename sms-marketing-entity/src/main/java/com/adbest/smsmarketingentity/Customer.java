package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 客户 [本系统服务对象，比如商场老板]
 */
@Entity
@Data
public class Customer implements Serializable {
    
    @Id
    @GeneratedValue
    private Long id;  // 主键
    @Column(unique = true, nullable = false)
    private String email;  // 邮箱 (作为用户名)
    @Column(nullable = false)
    private String password;  // 密码
    @Column(nullable = false)
    private String firstName;  // 名字
    @Column(nullable = false)
    private String lastName;  // 姓氏
    @Column(nullable = false)
    private UsArea state;  // 州
    @Column(nullable = false)
    private UsArea city;  // 城市
    private String industry;  // 行业
    private String organization;  // 单位（公司/机构）
    @Column(nullable = false)
    private Timestamp registerTime;  // 注册时间
    @Column(nullable = false)
    private Boolean disable;  // 是否禁用
}
