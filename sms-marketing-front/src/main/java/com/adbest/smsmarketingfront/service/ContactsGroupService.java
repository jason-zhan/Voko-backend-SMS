package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.ContactsGroup;
import com.adbest.smsmarketingfront.entity.form.ContactsGroupForm;
import com.adbest.smsmarketingfront.entity.vo.PageDataVo;
import com.adbest.smsmarketingfront.util.PageBase;

import java.util.List;

public interface ContactsGroupService {

    ContactsGroup save(ContactsGroupForm contactsGroup);

    Long countByCustomerIdAndTitle(Long customerId, String title);

    Boolean checkName(Long customerId, String name);

    PageDataVo findAll(PageBase page);

    ContactsGroup update(ContactsGroupForm contactsGroupForm);

    Integer delete(List<String> groupIds);

    PageDataVo contacts(String id, PageBase pageBase);

    Long countByIdInAndCustomerId(List<Long> gids, Long customerId);

    ContactsGroup findById(Long groupId);

    List<ContactsGroup> findAll();

    List<Object> findByContentIn(List<Long> ids);
}
