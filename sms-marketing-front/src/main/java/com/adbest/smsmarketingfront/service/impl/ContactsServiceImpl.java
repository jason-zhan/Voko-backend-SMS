package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingfront.dao.ContactsDao;
import com.adbest.smsmarketingfront.service.ContactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ContactsServiceImpl implements ContactsService {

    @Autowired
    private ContactsDao contactsDao;


    @Override
    public Page<Contacts> findByContactsGroupId(String contactsGroupId, String page, String pageSize) {
        Pageable pageable = PageRequest.of(Integer.valueOf(page), Integer.valueOf(pageSize));
        return contactsDao.findByContactsGroupId(Long.valueOf(contactsGroupId), pageable);
    }
}
