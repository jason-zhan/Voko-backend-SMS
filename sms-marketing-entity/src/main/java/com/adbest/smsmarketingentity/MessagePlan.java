package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 消息定时任务
 */
@Entity
@Data
public class MessagePlan implements Serializable {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;  // 标题
    @Column(nullable = false)
    private Timestamp execTime;  // 执行时间
    private String remark;  // 备注
    @Column(nullable = false)
    private Boolean disable;  // 是否禁用(true:禁用)
    @Column(nullable = false)
    private Timestamp createTime;  // 创建时间
}
