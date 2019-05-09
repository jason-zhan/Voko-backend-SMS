package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 关键字
 * 【规定】同一用户关键字唯一
 */
@Data
@Entity
public class Keyword implements Serializable {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    /**
     * @see Customer#id
     */
    @Column(nullable = false)
    private String customerId;
    @ManyToOne
    @JoinColumn(nullable = false)
    private ServiceNumber serviceNumber;  // 服务短号
    @Column(nullable = false)
    private String title;  // 关键字名称
    @Lob
    private String content;  // 回复消息文本内容
    @Lob
    private String mediaIdList;  // 回复消息携带的媒体id列表
    private Timestamp updateTime;  // 最近修改时间
}
