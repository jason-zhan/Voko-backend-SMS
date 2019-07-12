package com.adbest.smsmarketingfront.entity.vo;

import com.adbest.smsmarketingentity.InboxStatus;
import com.adbest.smsmarketingentity.MessageRecord;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 收件箱消息展示实体
 * @see MessageRecord
 */
@Data
public class InboxMessageVo {
    
    private Long id;  // 消息id
    private Long customerId;  // 用户id
        private String customerNumber;  // 用户号码
    private String content;  // 消息内容
    //    private String mediaList;  // 资源列表 [资源url,多个以','分隔]
    private Boolean sms;  // 是否短信(true:是)
    private Long contactsId;  // 联系人id
    private String contactsNumber;  // 联系人号码
    private String contactsFirstName;  // 联系人名称
    private String contactsLastName;  // 联系人姓氏
    private Timestamp createTime;  // 创建时间
    /**
     * @see InboxStatus
     */
    private Integer status;  // 消息状态
    
    public InboxMessageVo() {
    }
    
    public InboxMessageVo(MessageRecord message, String contactsFirstName, String contactsLastName) {
        this.id = message.getId();
        this.customerId = message.getCustomerId();
        this.customerNumber = message.getCustomerNumber();
        this.content = message.getContent();
        this.sms = message.getSms();
        this.contactsId = message.getContactsId();
        this.contactsNumber = message.getContactsNumber();
        this.contactsFirstName = contactsFirstName;
        this.contactsLastName = contactsLastName;
        this.createTime = message.getCreateTime();
        this.status = message.getStatus();
    }
}
