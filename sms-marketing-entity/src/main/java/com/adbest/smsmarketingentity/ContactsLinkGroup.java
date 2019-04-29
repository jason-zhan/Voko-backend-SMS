package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 联系人与群组关系表
 */
@Entity
@Data
public class ContactsLinkGroup implements Serializable {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    /**
     * @see Contacts#id
     */
    @Column(nullable = false)
    private Long contactsId;
    /**
     * @see ContactsGroup#id
     */
    @Column(nullable = false)
    private Long groupId;
}
