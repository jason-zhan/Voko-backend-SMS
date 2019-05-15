package com.adbest.smsmarketingfront.entity.dto;

import lombok.Data;

@Data
public class CustomerDto {
    private Integer id;
    private String email;
    private String iCustomer;
    private String customerId;
    private String phone;

    public CustomerDto() {
    }

    public CustomerDto(Integer id, String phone, String email, String iCustomer, String customerId) {
        this.id = id;
        this.email = email;
        this.iCustomer = iCustomer;
        this.customerId = customerId;
        this.phone = phone;
    }
}
