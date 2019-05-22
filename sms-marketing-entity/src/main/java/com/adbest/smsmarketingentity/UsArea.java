package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 美国地区
 */
@Entity
@Data
@Table(name = "us_area")
public class UsArea implements Serializable {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private Long parentId;  // 上一级id
    private String title;  // 名称
    private String abbr;  // 缩写
}
