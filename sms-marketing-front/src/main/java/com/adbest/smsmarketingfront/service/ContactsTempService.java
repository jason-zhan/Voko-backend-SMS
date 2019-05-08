package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.ContactsTemp;
import com.adbest.smsmarketingfront.entity.form.ContactsImportForm;

import java.util.List;

public interface ContactsTempService {
    String importContacts(ContactsImportForm contactsImportForm);

    List<ContactsTemp> findByTempSign(String tempSign);
}
