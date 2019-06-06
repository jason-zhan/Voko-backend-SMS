package com.adbest.smsmarketingentity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 系统模板 (后台运营人员维护)
 */
@Entity
@Data
public class SystemMsgTemplate implements Serializable {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Boolean sms;  // 是否短信模板(true:是)
    /**
     * 模板类型
     * @see SystemMsgTemplateType
     */
    @Column(nullable = false)
    private Integer type;
    @Column(nullable = false)
    private String title;  // 标题
    protected String subject;  // 主题
    @Column(nullable = false)
    @Lob
    protected String content;  // 内容
    @Lob
    private String mediaList;  // 资源id列表 [多个以','分隔]
    private String remark;  // 备注
    @Column(nullable = false)
    private Boolean disable;  // 是否禁用(true:是)
    @Column(nullable = false)
    @CreationTimestamp
    private Timestamp createTime;  // 创建时间
    @Column(nullable = false)
    @UpdateTimestamp
    private Timestamp updateTime;  // 更新时间
    @Column(nullable = false)
    private Long updateBy;  // 最后修改人
}
