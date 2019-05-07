package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.ContactsLinkGroup;

import java.util.List;

public interface ContactsLinkGroupService {

    List<ContactsLinkGroup> findByGroupIdIn(List<Long> groupIds);

    void saveAll(List<ContactsLinkGroup> list);

    Integer deleteByContactsGroupIdIn(List<Long> ids);

    void createContactsLinkGroup(List<Long> contactsIds, List<Long> groupIds);

    void deleteByContactsIdIn(List<Long> asList);
}
