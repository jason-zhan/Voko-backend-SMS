package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingfront.dao.ContactsDao;
import com.adbest.smsmarketingfront.entity.enums.ContactsSource;
import com.adbest.smsmarketingfront.entity.form.ContactsForm;
import com.adbest.smsmarketingfront.entity.vo.ContactsVo;
import com.adbest.smsmarketingfront.entity.vo.PageDataVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.ContactsGroupService;
import com.adbest.smsmarketingfront.service.ContactsLinkGroupService;
import com.adbest.smsmarketingfront.service.ContactsService;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.ErrorMag;
import com.adbest.smsmarketingfront.util.PageBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContactsServiceImpl implements ContactsService {

    @Autowired
    private ContactsDao contactsDao;

    @Autowired
    private ContactsLinkGroupService contactsLinkGroupService;

    @Autowired
    private ContactsGroupService contactsGroupService;

    @Override
    public PageDataVo findByContactsGroupId(String contactsGroupId, PageBase pageBase) {
        Pageable pageable = PageRequest.of(pageBase.getPage(), pageBase.getSize());
        Page<Contacts> page = contactsDao.findByContactsGroupId(Long.valueOf(contactsGroupId), pageable);
        return new PageDataVo(page);
    }

    @Override
    @Transactional
    public ContactsVo save(ContactsForm contactsForm) {
        Long customerId = Current.getUserDetails().getId();
        Contacts contacts = contactsForm.getContacts();
        Contacts ct = contactsDao.findFirstByPhoneAndCustomerId(contactsForm.getPhone(), customerId);
        if (ct==null){
            contacts.setIsDelete(false);
            contacts.setInLock(false);
            contacts.setCustomerId(customerId);
            contacts.setSource(ContactsSource.Manually_Added.getValue());
        }else {
            contacts = contactsForm.getContacts(ct);
        }
        contactsDao.save(contacts);
        if (contactsForm.getGroupIds()!=null){
            List<Long> gids = contactsForm.getGroupIds().stream().map(s -> Long.valueOf(s)).collect(Collectors.toList());
            Long count = contactsGroupService.countByIdInAndCustomerId(gids, customerId);
            ServiceException.isTrue(count==gids.size(), ErrorMag.GROUP_INFO_ERROR);
            contactsLinkGroupService.deleteByContactsIdIn(Arrays.asList(contacts.getId()));
            contactsLinkGroupService.createContactsLinkGroup(Arrays.asList(contacts.getId()), gids);
        }
        return new ContactsVo(contacts);
    }

    @Override
    @Transactional
    public Integer delete(String ids) {
        ServiceException.notNull(ids, ErrorMag.CONTACTS_NOT_EMPTY);
        Long customerId = Current.getUserDetails().getId();
        List<String> id = Arrays.asList(ids.split(","));
        List<Long> idList = id.stream().map(s -> Long.valueOf(s)).collect(Collectors.toList());
        Integer row = contactsDao.updateIsDisableByCustomerIdAndIdIn(customerId, idList);
        ServiceException.isTrue(row==idList.size(),ErrorMag.CONTACTS_NOT_EXISTS);
        contactsLinkGroupService.deleteByContactsIdIn(idList);
        return row;
    }

    @Override
    @Transactional
    public Boolean updateLock(String id, Boolean isLock) {
        ServiceException.notNull(id, ErrorMag.CONTACTS_NOT_EMPTY);
        ServiceException.notNull(isLock, ErrorMag.INLOCK_NOT_EMPTY);
        Long customerId = Current.getUserDetails().getId();
        Optional<Contacts> Optional = contactsDao.findById(Long.valueOf(id));
        ServiceException.isTrue(Optional.isPresent(), ErrorMag.CONTACTS_NOT_EXISTS);
        Contacts contacts = Optional.get();
        ServiceException.isTrue(contacts.getCustomerId()==customerId, ErrorMag.CONTACTS_NOT_EXISTS);
        contacts.setIsDelete(isLock);
        contacts.setInLockTime(new Timestamp(System.currentTimeMillis()));
        contactsDao.save(contacts);
        return true;
    }
}
