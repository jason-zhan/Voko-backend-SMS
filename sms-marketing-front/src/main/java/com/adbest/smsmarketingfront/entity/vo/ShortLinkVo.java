package com.adbest.smsmarketingfront.entity.vo;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ShortLinkVo {
    private Long id;  // 主键

    private String base_url; //域名
    private String suffix_url;//链接除了域名外的后缀
    private String shortURL; //当前 suffix_url 链接的短码
    private String fullURL; //完整链接
    private Timestamp expiration_date;//失效日期
    private long total_click_count;//当前链接总点击次数
}
