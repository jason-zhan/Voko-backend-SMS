package com.adbest.smsmarketingfront.entity.form;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingentity.ContactsTemp;
import lombok.Data;

import java.util.List;

@Data
public class ContactsForm {

    private String id;
    private String phone; // 号码
    private String firstName;  // 名字
    private String lastName;  // 姓氏
    private String email;  // 邮箱
    private String notes;  // 备注
    List<String> groupIds;

    public ContactsForm() {
    }

    public Contacts getContacts(){
        Contacts contacts = new Contacts();
        contacts.setEmail(email);
        contacts.setFirstName(firstName);
        contacts.setLastName(lastName);
        contacts.setNotes(notes);
        contacts.setPhone(phone);
        return contacts;
    }

    public Contacts getContacts(Contacts contacts){
        contacts.setEmail(email);
        contacts.setFirstName(firstName);
        contacts.setLastName(lastName);
        contacts.setNotes(notes);
        contacts.setPhone(phone);
        return contacts;
    }

    public ContactsTemp getContactsTemp(Long customerId, String tempSign) {
        ContactsTemp contacts = new ContactsTemp();
        contacts.setEmail(email);
        contacts.setFirstName(firstName);
        contacts.setLastName(lastName);
        contacts.setNotes(notes);
        contacts.setPhone(phone);
        contacts.setCustomerId(customerId);
        contacts.setTempSign(tempSign);
        return contacts;
    }
}
