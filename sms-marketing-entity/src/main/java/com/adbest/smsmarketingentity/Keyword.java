package com.adbest.smsmarketingentity;

import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

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
    private Long customerId;
    @Column(nullable = false)
    private String title;  // 关键字名称
    @Lob
    private String content;  // 回复消息文本内容
    @Lob
    private String mediaIdList;  // 回复消息携带的媒体id列表
    @UpdateTimestamp
    private Timestamp updateTime;  // 最近修改时间

    /**
     * 是否为赠送关键字
     */
    private Boolean giftKeyword;

    public Keyword() {
    }

    public Keyword(Long customerId, String title) {
        this.customerId = customerId;
        this.title = title;
    }
}
