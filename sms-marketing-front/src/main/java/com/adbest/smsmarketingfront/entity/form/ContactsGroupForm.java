package com.adbest.smsmarketingfront.entity.form;

import com.adbest.smsmarketingentity.ContactsGroup;
import lombok.Data;

import java.util.List;

@Data
public class ContactsGroupForm {

    Long id;

    String name;

    List<String> groupIds;

    Long customerId;

    public ContactsGroup getContactsGroup() {
        ContactsGroup contactsGroup = new ContactsGroup();
        contactsGroup.setTitle(this.name);
        contactsGroup.setCustomerId(this.getCustomerId());
        return contactsGroup;
    }
}
