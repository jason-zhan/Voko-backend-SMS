package com.adbest.smsmarketingfront.entity.vo;

import com.adbest.smsmarketingentity.MobileNumber;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class MobileNumberVo implements Serializable {
    private Long id;
    private String number;
    private Boolean sms;
    private Boolean mms;
    private Timestamp createTime;

    public MobileNumberVo(MobileNumber mobileNumber) {
        this.id = mobileNumber.getId();
        this.number = mobileNumber.getNumber();
        this.sms = mobileNumber.getSms();
        this.mms = mobileNumber.getMms();
        this.createTime = mobileNumber.getCreateTime();
    }

    public MobileNumberVo() {
    }
}
