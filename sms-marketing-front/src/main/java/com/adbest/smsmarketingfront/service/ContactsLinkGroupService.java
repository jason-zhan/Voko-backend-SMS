package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.ContactsLinkGroup;
import com.adbest.smsmarketingfront.entity.form.AddContactsToGroupsForm;

import java.util.List;

public interface ContactsLinkGroupService {

    List<ContactsLinkGroup> findByGroupIdIn(List<Long> groupIds);

    Integer deleteByContactsGroupIdIn(List<Long> ids);

    void createContactsLinkGroup(List<Long> contactsIds, Long groupId);

    void deleteByContactsIdIn(List<Long> asList);

    void createContactsLinkGroup(Long contactsIds, List<Long> groupId);

    void deleteByContactsGroupIdAndContactsIdIn(Long groupId, List<Long> contactsIds);
}
