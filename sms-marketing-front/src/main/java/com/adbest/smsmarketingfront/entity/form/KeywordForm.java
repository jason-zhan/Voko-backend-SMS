package com.adbest.smsmarketingfront.entity.form;

import lombok.Data;

@Data
public class KeywordForm {
    private Long id;
    private String title;  // 关键字名称
    private String content;  // 回复消息文本内容
    private String mediaIdList;  // 回复消息携带的媒体id列表
}
