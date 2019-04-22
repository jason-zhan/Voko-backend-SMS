package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 美国地区
 */
@Entity
@Data
public class UsArea implements Serializable {
    
    @Id
    @GeneratedValue
    private Long id;
    private Long parentId;  // 上一级id
    private String title;  // 名称
    private String abbr;  // 缩写
}
