package com.adbest.smsmarketingentity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 消息发送任务
 */
@Entity
@Data
@Table(name = "message_plan")
public class MessagePlan implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private Boolean isSms;  // 是否短信(true:是)
    @Column(nullable = false)
    private Timestamp execTime;  // 执行时间
    private String remark;  // 备注
    private String fromNumList;  // 发送消息的号码列表，多个以','分隔
    private String toNumList; // 接收消息的联系人号码列表，多个以','分隔
    private String toGroupList;  // 接收消息的群组id列表，多个以','分隔
    @Column(nullable = false)
    private Integer msgTotal;  // 消息总数
    @Column(nullable = false)
    private Integer creditPayNum;  // 额外支付数目
    @Column(nullable = false)
    private BigDecimal creditPayCost;  // 额外支付金额
    @Column(nullable = false)
    private Boolean disable;  // 是否禁用(true:禁用)
    /**
     * 任务状态
     * @see MessagePlanStatus
     */
    private Integer status;
    @Column(nullable = false)
    @CreationTimestamp
    private Timestamp createTime;  // 创建时间
    @Column(nullable = false)
    @UpdateTimestamp
    private Timestamp updateTime;  // 更新时间
}
