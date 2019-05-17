package com.adbest.smsmarketingfront.entity.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MobileNumberVo implements Serializable {
    private Long id;
    private String number;

    public MobileNumberVo(Long id, String number) {
        this.id = id;
        this.number = number;
    }

    public MobileNumberVo() {
    }
}
