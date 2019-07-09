package com.adbest.smsmarketingfront.entity.dto;

import lombok.Data;

@Data
public class ContactsDto {
    private Long id;
    private Long customerId;
    private String phone;
    private Integer vkCDRAccountsId;
    private String content;
    private String firstName;
    private String lastName;
    private String customeFirstName;
    private String customerLastName;
    private String callId;

    public ContactsDto(Long id, Long customerId, String phone, Integer vkCDRAccountsId, String content, String firstName, String lastName, String customeFirstName,
                       String customerLastName,String callId) {
        this.id = id;
        this.customerId = customerId;
        this.phone = phone;
        this.vkCDRAccountsId = vkCDRAccountsId;
        this.content = content;
        this.firstName = firstName;
        this.lastName = lastName;
        this.customeFirstName = customeFirstName;
        this.customerLastName = customerLastName;
        this.callId = callId;
    }

    public ContactsDto() {
    }
}
