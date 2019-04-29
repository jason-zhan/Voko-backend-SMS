package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 消息记录
 */
@Entity
@Data
public class MessageRecord implements Serializable {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
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
    private String content;
    @Column(nullable = false)
    private Integer segments;  // 被分割为多少条消息
    @Column(nullable = false)
    private String mediaList;  // 资源列表 [资源url,多个以','分隔]
    /**
     * @see Contacts#id
     */
    @Column(nullable = false)
    private Long contactsId;
    @Column(nullable = false)
    private String contactsNumber;  // 联系人号码
    @Column(nullable = false)
    private Boolean inbox; // 是否收件（true:是）
    @Column(nullable = false)
    private Timestamp createTime;  // 创建时间
    private Timestamp sendTime;  // 发送时间
    private Timestamp arrivedTime;  // 送达时间
    private Integer returnCode;  // 状态码
}
