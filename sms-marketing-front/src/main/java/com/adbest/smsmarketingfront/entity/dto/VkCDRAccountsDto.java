package com.adbest.smsmarketingfront.entity.dto;

import lombok.Data;

@Data
public class VkCDRAccountsDto {

    private Integer id;

    private String CLI;

    private String call_id;

    private Long customerId;

    private Long contactsId;

    public VkCDRAccountsDto(Integer id, String CLI, String call_id, Long customerId, Long contactsId) {
        this.id = id;
        this.CLI = CLI;
        this.call_id = call_id;
        this.customerId = customerId;
        this.contactsId = contactsId;
    }

    public VkCDRAccountsDto() {
    }
}
