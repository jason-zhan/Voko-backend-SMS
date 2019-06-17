package com.adbest.smsmarketingfront.entity.vo;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.OutboxStatus;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 发件箱消息展示实体
 *
 * @see MessageRecord
 */
@Data
public class OutboxMessageVo {
    
    private Long id;  // 消息id
    private Long planId;  // 计划id
    private Long customerId;  // 用户id
    private String customerNumber;  // 用户号码
    private String content;  // 消息内容
    private Integer segments;  // 被分割为多少条消息
    //    private String mediaList;  // 资源列表 [资源url,多个以','分隔]
    private Boolean sms;  // 是否短信(true:是)
    private Long contactsId;  // 联系人id
    private String contactsNumber;  // 联系人号码
    private String contactsFirstName;  // 联系人名称
    private String contactsLastName;  // 联系人姓氏
    private Timestamp createTime;  // 创建时间
    private Timestamp sendTime;  // 发送时间
    private Timestamp arrivedTime;  // 送达时间
    /**
     * @see OutboxStatus
     */
    private Integer status;  // 消息状态
    
    public OutboxMessageVo() {
    }
    
    public OutboxMessageVo(MessageRecord message, String contactsFirstName, String contactsLastName) {
        this.id = message.getId();
        this.planId = message.getPlanId();
        this.customerId = message.getCustomerId();
        this.customerNumber = message.getCustomerNumber();
        this.content = message.getContent();
        this.segments = message.getSegments();
        this.sms = message.getSms();
        this.contactsId = message.getContactsId();
        this.contactsNumber = message.getContactsNumber();
        this.contactsFirstName = contactsFirstName;
        this.contactsLastName = contactsLastName;
        this.createTime = message.getCreateTime();
        this.sendTime = message.getSendTime();
        this.arrivedTime = message.getArrivedTime();
        this.status = message.getStatus();
    }
}
