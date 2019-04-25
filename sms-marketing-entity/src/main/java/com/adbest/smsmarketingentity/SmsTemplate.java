package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 短信模板
 */
@Entity
@Data
public class SmsTemplate implements Serializable {
    
    @Id
    @GeneratedValue
    private Long id;
    protected String subject;  // 主题
    @Lob
    protected String content;  // 内容
    protected Timestamp createTime;  // 创建时间
    protected Timestamp updateTime;  // 更新时间
}
