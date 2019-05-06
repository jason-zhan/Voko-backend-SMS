package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.Contacts;
import org.springframework.data.domain.Page;

public interface ContactsService {

    Page<Contacts> findByContactsGroupId(String contactsGroupId, String page, String pageSize);
}
