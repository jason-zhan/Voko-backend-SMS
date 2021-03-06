package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 联系人与群组关系表
 */
@Entity
@Data
@Table(name = "contacts_link_group")
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

    public ContactsLinkGroup(Long contactsId, Long groupId) {
        this.contactsId = contactsId;
        this.groupId = groupId;
    }

    public ContactsLinkGroup() {
    }
}
