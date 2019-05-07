package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingentity.ContactsGroup;
import com.adbest.smsmarketingfront.entity.vo.ContactsGroupForm;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ContactsGroupService {

    ContactsGroup save(ContactsGroupForm contactsGroup);

    Long countByCustomerIdAndTitle(Long customerId, String title);

    Boolean checkName(Long customerId, String name);

    Page<ContactsGroup> findAll(String page, String pageSize);

    ContactsGroup update(ContactsGroupForm contactsGroupForm);

    Integer delete(List<String> groupIds);

    Page<Contacts> contacts(String id, String page, String pageSize);
}
