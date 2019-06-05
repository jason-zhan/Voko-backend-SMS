package com.adbest.smsmarketingfront.entity.form;

import lombok.Data;

@Data
public class SearchTwilioForm {
    /**
     * 区号
     */
    private String areaCode;
    /**
     * 内容
     */
    private String contains;
    /**
     * 城市名称
     */
    private String inRegion;
}
