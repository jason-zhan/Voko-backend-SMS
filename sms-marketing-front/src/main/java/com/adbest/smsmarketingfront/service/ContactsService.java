package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingfront.entity.form.ContactsForm;
import com.adbest.smsmarketingfront.entity.vo.ContactsVo;
import com.adbest.smsmarketingfront.entity.vo.PageDataVo;
import com.adbest.smsmarketingfront.util.PageBase;

import java.util.List;

public interface ContactsService {

    PageDataVo findByContactsGroupId(String contactsGroupId, PageBase pageBase);

    ContactsVo save(ContactsForm contactsForm);

    Integer delete(String ids);

    Boolean updateLock(String id, Boolean isLock);
}
