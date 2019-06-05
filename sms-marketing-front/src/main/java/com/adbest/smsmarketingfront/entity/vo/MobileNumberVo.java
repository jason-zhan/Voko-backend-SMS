package com.adbest.smsmarketingfront.entity.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MobileNumberVo implements Serializable {
    private Long id;
    private String number;
    private Boolean sms;
    private Boolean mms;

    public MobileNumberVo(Long id, String number, Boolean sms, Boolean mms) {
        this.id = id;
        this.number = number;
        this.sms = sms;
        this.mms = mms;
    }

    public MobileNumberVo() {
    }
}
