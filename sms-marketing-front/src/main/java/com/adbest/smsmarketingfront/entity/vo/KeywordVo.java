package com.adbest.smsmarketingfront.entity.vo;

import com.adbest.smsmarketingentity.Keyword;
import lombok.Data;

import java.io.Serializable;

@Data
public class KeywordVo implements Serializable {
    private Long id;
    private String title;  // 关键字名称
    private String content;  // 回复消息文本内容
    private String mediaIdList;  // 回复消息携带的媒体id列表

    public KeywordVo() {
    }

    public KeywordVo(Keyword keyword) {
        this.id = keyword.getId();
        this.title = keyword.getTitle();
        this.content = keyword.getContent();
        this.mediaIdList = keyword.getMediaIdList();
    }
}
