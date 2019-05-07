package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingentity.ContactsGroup;
import com.adbest.smsmarketingentity.ContactsLinkGroup;
import com.adbest.smsmarketingfront.dao.ContactsGroupDao;
import com.adbest.smsmarketingfront.entity.vo.ContactsGroupForm;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.ContactsGroupService;
import com.adbest.smsmarketingfront.service.ContactsLinkGroupService;
import com.adbest.smsmarketingfront.service.ContactsService;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.ErrorMag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContactsGroupServiceImpl implements ContactsGroupService {

    @Autowired
    private ContactsGroupDao contactsGroupDao;

    @Autowired
    private ContactsLinkGroupService contactsLinkGroupService;

    @Autowired
    private ContactsService contactsService;

    @Override
    @Transactional
    public ContactsGroup save(ContactsGroupForm contactsGroup) {
        ServiceException.notNull(contactsGroup.getName(), ErrorMag.GROUP_NAME_NOT_EMPTY);
        Long count = countByCustomerIdAndTitle(contactsGroup.getCustomerId(), contactsGroup.getName());
        ServiceException.isTrue(count<=0, ErrorMag.GROUP_NAME_EXISTS);
        ContactsGroup co = contactsGroupDao.save(contactsGroup.getContactsGroup());
        List<String> groupIds = contactsGroup.getGroupIds();
        if (groupIds!=null && groupIds.size()>0){
            List<Long> ids = groupIds.stream().map(s -> Long.valueOf(s)).collect(Collectors.toList());
            count = contactsGroupDao.countByIdInAndCustomerId(ids, contactsGroup.getCustomerId());
            ServiceException.isTrue(count==ids.size(), ErrorMag.GROUP_INFO_ERROR);
            List<ContactsLinkGroup> contactsLinkGroups = contactsLinkGroupService.findByGroupIdIn(ids);
            if (contactsLinkGroups.size()>0){
                List<Long> contactsIds = contactsLinkGroups.stream().map(s -> s.getContactsId()).distinct().collect(Collectors.toList());
                List<ContactsLinkGroup> list = new ArrayList<>();
                ContactsLinkGroup clg = null;
                for (Long id:contactsIds) {
                    clg = new ContactsLinkGroup(id, co.getId());
                    list.add(clg);
                }
                contactsLinkGroupService.saveAll(list);
            }
        }
        return co;
    }

    @Override
    public Long countByCustomerIdAndTitle(Long customerId, String title) {
        return contactsGroupDao.countByCustomerIdAndTitle(customerId, title);
    }

    @Override
    public Boolean checkName(Long customerId, String name) {
        return countByCustomerIdAndTitle(customerId, name)<=0;
    }

    @Override
    public Page<ContactsGroup> findAll(String page, String pageSize) {
        Pageable pageable = PageRequest.of(Integer.valueOf(page), Integer.valueOf(pageSize));
        Page<ContactsGroup> data = contactsGroupDao.findByCustomerIdAndIsDelete(Current.getUserDetails().getId(), false, pageable);
        return data;
    }

    @Override
    @Transactional
    public ContactsGroup update(ContactsGroupForm contactsGroupForm) {
        Long count = countByCustomerIdAndTitle(Current.getUserDetails().getId(), contactsGroupForm.getName());
        ServiceException.isTrue(count<=0, ErrorMag.GROUP_NAME_EXISTS);
        Optional<ContactsGroup> optionalContactsGroup = contactsGroupDao.findById(contactsGroupForm.getId());
        ServiceException.isTrue(optionalContactsGroup.isPresent(),ErrorMag.GROUP_INFO_NOT_EXISTS);
        ServiceException.isTrue(optionalContactsGroup.get().getCustomerId()== Current.getUserDetails().getId(),ErrorMag.GROUP_INFO_NOT_EXISTS);
        ContactsGroup contactsGroup = optionalContactsGroup.get();
        contactsGroup.setTitle(contactsGroupForm.getName());
        return contactsGroupDao.save(contactsGroup);
    }

    @Override
    @Transactional
    public Integer delete(List<String> groupIds) {
        ServiceException.isTrue(groupIds!=null, ErrorMag.GROUP_INFO_NOT_EXISTS);
        Long customerId = Current.getUserDetails().getId();
        List<Long> ids = groupIds.stream().map(s -> Long.valueOf(s)).collect(Collectors.toList());
        Long count = contactsGroupDao.countByIdInAndCustomerId(ids, customerId);
        ServiceException.isTrue(count==ids.size(), ErrorMag.GROUP_INFO_ERROR);
        Integer row = contactsGroupDao.deleteByIdIn(ids);
        contactsLinkGroupService.deleteByContactsGroupIdIn(ids);
        return row;
    }

    @Override
    public Page<Contacts> contacts(String id, String page, String pageSize) {
        return contactsService.findByContactsGroupId(id, page, pageSize);
    }

}
