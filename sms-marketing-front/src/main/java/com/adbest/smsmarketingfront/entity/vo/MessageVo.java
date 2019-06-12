package com.adbest.smsmarketingfront.entity.vo;

import com.adbest.smsmarketingentity.MessageRecord;
import lombok.Data;

@Data
public class MessageVo extends MessageRecord {
    
    private String contactsFirstName;  // 联系人名字
    private String contactsLastName;  // 联系人姓氏
    private String contactsGroupTitle;  // 联系人分组名称
    
    public MessageVo(MessageRecord message, String contactsFirstName, String contactsLastName, String contactsGroupTitle) {
        this(message, contactsFirstName, contactsLastName);
        this.contactsGroupTitle = contactsGroupTitle;
    }
    
    public MessageVo(MessageRecord message, String contactsFirstName, String contactsLastName) {
        this.setId(message.getId());
        this.setPlanId(message.getPlanId());
        this.setCustomerId(message.getCustomerId());
        this.setCustomerNumber(message.getCustomerNumber());
        this.setContent(message.getContent());
        this.setSegments(message.getSegments());
        this.setMediaList(message.getMediaList());
        this.setSms(message.getSms());
        this.setContactsId(message.getContactsId());
        this.setContactsNumber(message.getContactsNumber());
        this.setContactsGroupId(message.getContactsGroupId());
        this.setCreateTime(message.getCreateTime());
        this.setSendTime(message.getSendTime());
        this.setExpectedSendTime(message.getExpectedSendTime());
        this.setArrivedTime(message.getArrivedTime());
        this.setStatus(message.getStatus());
        this.contactsFirstName = contactsFirstName;
        this.contactsLastName = contactsLastName;
    }
    
    public MessageVo() {
    }
}
