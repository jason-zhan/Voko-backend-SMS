package com.adbest.smsmarketingentity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    private Long customerId;  // 用户id
    @Column(nullable = false)
    private String title;  // 标题
    @Column(nullable = false)
    @Lob
    private String text;  // 内容
    @Lob
    private String mediaIdList;  // 媒体id列表，多个以','分隔
    @Column(nullable = false)
    private Timestamp execTime;  // 执行时间
    private String remark;  // 备注
    @Column(nullable = false)
    private Boolean disable;  // 是否禁用(true:禁用)
    /**
     * 定时任务状态
     * @see MessagePlanStatus
     */
    private Integer status;
    @Column(nullable = false)
    @CreationTimestamp
    private Timestamp createTime;  // 创建时间
    @UpdateTimestamp
    private Timestamp updateTime;  // 更新时间
}
