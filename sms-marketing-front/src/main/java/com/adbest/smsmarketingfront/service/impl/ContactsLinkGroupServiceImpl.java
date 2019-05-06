package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.ContactsLinkGroup;
import com.adbest.smsmarketingfront.dao.ContactsLinkGroupDao;
import com.adbest.smsmarketingfront.service.ContactsLinkGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ContactsLinkGroupServiceImpl implements ContactsLinkGroupService {

    @Autowired
    private ContactsLinkGroupDao contactsLinkGroupDao;

    @Override
    public List<ContactsLinkGroup> findByGroupIdIn(List<Long> groupIds) {
        return contactsLinkGroupDao.findByGroupIdIn(groupIds);
    }

    @Override
    @Transactional
    public void saveAll(List<ContactsLinkGroup> list) {
        contactsLinkGroupDao.saveAll(list);
    }

    @Override
    public Integer deleteByContactsGroupIdIn(List<Long> ids) {
        return contactsLinkGroupDao.deleteByContactsGroupIdIn(ids);
    }
}
