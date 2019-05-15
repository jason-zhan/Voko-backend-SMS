package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingfront.entity.form.AddContactsToGroupsForm;
import com.adbest.smsmarketingfront.entity.form.ContactsForm;
import com.adbest.smsmarketingfront.entity.form.ContactsProcessForm;
import com.adbest.smsmarketingfront.entity.form.SelectContactsForm;
import com.adbest.smsmarketingfront.entity.vo.ContactsVo;
import com.adbest.smsmarketingfront.entity.vo.PageDataVo;
import com.adbest.smsmarketingfront.util.PageBase;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContactsService {

    PageDataVo findByContactsGroupId(String contactsGroupId, PageBase pageBase);

    ContactsVo save(ContactsForm contactsForm);

    Integer delete(String ids);

    Boolean updateLock(String id, Boolean isLock);

    Boolean addContactsToGroups(AddContactsToGroupsForm addContactsToGroupsForm);

    Boolean outContactsToGroups(AddContactsToGroupsForm addContactsToGroupsForm);

    Long countByIdInAndCustomerId(List<Long> contactsIds, Long customerId);

    PageDataVo select(SelectContactsForm selectContactsForm);

    PageDataVo selectAll(SelectContactsForm selectContactsForm);

    boolean process(ContactsProcessForm contactsProcessForm);

    List<ContactsVo> upload(MultipartFile file);

    void saveAll(List<Contacts> contactsList);
}
