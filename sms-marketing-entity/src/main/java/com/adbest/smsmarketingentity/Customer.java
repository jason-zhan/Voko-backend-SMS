package com.adbest.smsmarketingentity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 客户 [本系统服务对象，比如商场老板]
 */
@Entity
@Data
public class Customer implements Serializable {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;  // 主键
    @Column(unique = true, nullable = false)
    private String email;  // 邮箱 (作为用户名)
    @Column(nullable = false)
    private String password;  // 密码
    private String firstName;  // 名字
    private String lastName;  // 姓氏
    private UsArea state;  // 州
    private UsArea city;  // 城市
    private String industry;  // 行业
    private String organization;  // 单位（公司/机构）
    @Column(nullable = false)
    @CreationTimestamp
    private Timestamp registerTime;  // 注册时间
    @Column(nullable = false)
    private Boolean disable;  // 是否禁用

    private Integer source;  // 来源

    public static boolean checkEmail(String email) {
//        Pattern pattern = Pattern.compile("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$", Pattern.CASE_INSENSITIVE);
        Pattern pattern = Pattern.compile("^[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean checkPassword(String password) {
        Pattern pattern = Pattern.compile("^[A-Z0-9]{5,25}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
