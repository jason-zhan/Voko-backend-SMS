package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.ContactsLinkGroup;
import com.adbest.smsmarketingfront.dao.ContactsLinkGroupDao;
import com.adbest.smsmarketingfront.service.ContactsLinkGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    @Transactional
    public Integer deleteByContactsGroupIdIn(List<Long> ids) {
        return contactsLinkGroupDao.deleteByContactsGroupIdIn(ids);
    }

    @Override
    @Transactional
    public void createContactsLinkGroup(List<Long> contactsIds, List<Long> groupIds) {
        List<ContactsLinkGroup> list = new ArrayList<>();
        ContactsLinkGroup contactsLinkGroup = null;
        for (Long contactsId : contactsIds) {
            for (Long groupId : groupIds) {
                contactsLinkGroup = new ContactsLinkGroup(contactsId, groupId);
                list.add(contactsLinkGroup);
            }
        }
        contactsLinkGroupDao.saveAll(list);
    }

    @Override
    @Transactional
    public void deleteByContactsIdIn(List<Long> asList) {
        contactsLinkGroupDao.deleteByContactsIdIn(asList);
    }
}
