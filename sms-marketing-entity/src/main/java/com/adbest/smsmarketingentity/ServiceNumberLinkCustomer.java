package com.adbest.smsmarketingentity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 服务短号与用户关联表
 */
public class ServiceNumberLinkCustomer implements Serializable {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    /**
     * @see ServiceNumber#id
     */
    @Column(nullable = false)
    private Long numberId;
    /**
     * @see Customer#id
     */
    @Column(nullable = false)
    private Long customerId;
}
