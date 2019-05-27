package com.adbest.smsmarketingfront.entity.form;

import lombok.Data;

@Data
public class CustomerSettingsForm {

    private Boolean callReminder;

    private String content;
}
