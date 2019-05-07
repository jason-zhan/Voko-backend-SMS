package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.ContactsLinkGroup;

import java.util.List;

public interface ContactsLinkGroupService {

    List<ContactsLinkGroup> findByGroupIdIn(List<Long> groupIds);

    void saveAll(List<ContactsLinkGroup> list);

    Integer deleteByContactsGroupIdIn(List<Long> ids);
}
