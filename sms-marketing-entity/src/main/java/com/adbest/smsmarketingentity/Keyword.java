package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 关键字
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
    /**
     * @see MbNumberLib#number
     */
    @Column(nullable = false)
    private String number;
    @Column(nullable = false)
    private String title;  // 关键字名称
    private String content;  // 文字内容
    private Timestamp updateTime;  // 最近修改时间
}
