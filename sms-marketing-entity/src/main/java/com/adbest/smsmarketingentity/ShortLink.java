package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;

/*
短网址基础数据采用域名和后缀分开存储的形式。另外域名需要区分 HTTP 和 HTTPS
域名单独保存可以用于分析当前域名下链接的使用情况。
增加当前链接有效期字段，一般有短链需求的可能是相关活动或者热点事件，这种短链在一段时间内会很活跃，过了一定时间热潮会持续衰退。所以没有必要将这种链接永久保存增加每次查询的负担。
对于过期数据的处理，可以在新增短链的时候判断当前短链的失效日期
 */
@Entity
@Data
public class ShortLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 主键

    private String base_url; //域名
    private String suffix_url;//链接除了域名外的后缀
    private String shortURL; //当前 suffix_url 链接的短码
    private String fullURL; //完整链接
    private Timestamp expiration_date;//失效日期
    private long total_click_count;//当前链接总点击次数
}
