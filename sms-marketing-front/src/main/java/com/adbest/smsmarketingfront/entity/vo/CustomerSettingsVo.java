package com.adbest.smsmarketingfront.entity.vo;

import com.adbest.smsmarketingentity.CustomerSettings;
import lombok.Data;

@Data
public class CustomerSettingsVo {

    private Boolean callReminder;

    private String content;

    public CustomerSettingsVo() {
    }

    public CustomerSettingsVo(CustomerSettings customerSettings) {
        this.callReminder = customerSettings.getCallReminder();
        this.content = customerSettings.getContent();
    }
}
