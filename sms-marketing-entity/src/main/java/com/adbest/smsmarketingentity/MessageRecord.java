package com.adbest.smsmarketingentity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 消息记录
 */
@Entity
@Data
@Table(name = "message_record")
public class MessageRecord implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String sid;  // twilio 平台消息唯一标识
    /**
     * @see MessagePlan#id
     */
    private Long planId;
    /**
     * @see Customer#id
     */
    @Column(nullable = false)
    private Long customerId;
    @Column(nullable = false)
    private String customerNumber;  // 客户号码
    @Column(nullable = false)
    private Long contactsId;
    @Column(nullable = false)
    private String contactsNumber;  // 联系人号码
    @Column(nullable = false)
    private Boolean inbox; // 是否收件（true:是）
    @Column(nullable = false)
    private Boolean sms;  // 是否短信(true:是)
    @Lob
    @Column(nullable = false)
    private String content;  // 消息内容
    @Lob
    private String mediaList;  // 资源列表 [资源id,多个以','分隔]
    @Column(nullable = false)
    private Integer segments;  // 被分割为多少条消息
    @Column(columnDefinition = "decimal(10,3)",nullable = false)
    private BigDecimal cost;  // 花费(套餐内：0)(暂用单位：$)
    /**
     * @see InboxStatus
     * @see OutboxStatus
     */
    @Column(nullable = false)
    private Integer status;  // 消息状态
    /**
     * @see MessageReturnCode
     */
    private Integer returnCode;  // 状态码
    @Column(nullable = false)
    private Boolean disable;  // 是否禁用(true:是)
    @Column(nullable = false)
    @CreationTimestamp
    private Timestamp createTime;  // 创建时间
    private Timestamp sendTime;  // 发送时间
    private Timestamp arrivedTime;  // 送达时间
}
