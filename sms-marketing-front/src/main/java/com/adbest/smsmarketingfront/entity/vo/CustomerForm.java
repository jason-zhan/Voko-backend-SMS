package com.adbest.smsmarketingfront.entity.vo;

import lombok.Data;

@Data
public class CustomerForm {
    private String email;  // 邮箱 (作为用户名)
    private String password;  // 密码
    private String firstName;  // 名字
    private String lastName;  // 姓氏
    /**
     * 用户姓名
     */
    private String customerName;
    private String state;  // 州
    private String city;  // 城市
    private String industry;  // 行业
    private String organization;// 单位（公司/机构）
}
