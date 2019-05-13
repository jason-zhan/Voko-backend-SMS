package com.adbest.smsmarketingentity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 短信模板
 */
@Entity
@Data
public class MessageTemplate implements Serializable {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long customerId;  // 用户id
    @Column(nullable = false)
    private Boolean sms;  // 是否短信模板(true:是)
    protected String subject;  // 主题
    @Column(nullable = false)
    @Lob
    protected String content;  // 内容
    @Lob
    private String mediaList;  // 资源列表 [资源url,多个以','分隔]
    @Column(nullable = false)
    private Boolean disable;  // 是否禁用(true:是)
    @Column(nullable = false)
    @CreationTimestamp
    protected Timestamp createTime;  // 创建时间
    @Column(nullable = false)
    @UpdateTimestamp
    protected Timestamp updateTime;  // 更新时间
}
