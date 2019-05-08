package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.ContactsGroup;
import com.adbest.smsmarketingentity.ContactsLinkGroup;
import com.adbest.smsmarketingfront.dao.ContactsLinkGroupDao;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.ContactsGroupService;
import com.adbest.smsmarketingfront.service.ContactsLinkGroupService;
import com.adbest.smsmarketingfront.service.ContactsService;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class ContactsLinkGroupServiceImpl implements ContactsLinkGroupService {

    @Autowired
    private ContactsLinkGroupDao contactsLinkGroupDao;

    @Autowired
    private ContactsService contactsService;

    @Autowired
    private ContactsGroupService contactsGroupService;

    @Autowired
    private ReturnMsgUtil returnMsgUtil;

    @Override
    public List<ContactsLinkGroup> findByGroupIdIn(List<Long> groupIds) {
        return contactsLinkGroupDao.findByGroupIdIn(groupIds);
    }

    @Override
    @Transactional
    public Integer deleteByContactsGroupIdIn(List<Long> ids) {
        return contactsLinkGroupDao.deleteByContactsGroupIdIn(ids);
    }

    @Override
    @Transactional
    public void createContactsLinkGroup(List<Long> contactsIds, Long groupId) {
        checkParam(groupId, contactsIds);
        Map<Long,ContactsLinkGroup> map = new HashMap<>();
        ContactsLinkGroup contactsLinkGroup = null;
        for (Long contactsId : contactsIds) {
            contactsLinkGroup = new ContactsLinkGroup(contactsId, groupId);
            map.put(contactsId, contactsLinkGroup);
        }
        List<ContactsLinkGroup> list =  contactsLinkGroupDao.findByGroupIdAndContactsIdIn(groupId, contactsIds);
        for (ContactsLinkGroup clg:list) {
            map.remove(clg.getContactsId());
        }
        contactsLinkGroupDao.saveAll(map.values());
    }

    @Override
    @Transactional
    public void deleteByContactsIdIn(List<Long> asList) {
        contactsLinkGroupDao.deleteByContactsIdIn(asList);
    }

    @Override
    @Transactional
    public void createContactsLinkGroup(Long contactsId, List<Long> groupIds) {
        Long customerId = Current.getUserDetails().getId();
        Long count = contactsService.countByIdInAndCustomerId(Arrays.asList(contactsId), customerId);
        ServiceException.isTrue(count==1,returnMsgUtil.msg("CONTACTS_NOT_EXISTS"));
        count = contactsGroupService.countByIdInAndCustomerId(groupIds, customerId);
        ServiceException.isTrue(groupIds.size()==count, returnMsgUtil.msg("GROUP_INFO_ERROR"));
        Map<Long,ContactsLinkGroup> map = new HashMap<>();
        ContactsLinkGroup contactsLinkGroup = null;
        for (Long groupId : groupIds) {
            contactsLinkGroup = new ContactsLinkGroup(contactsId, groupId);
            map.put(groupId, contactsLinkGroup);
        }
        List<ContactsLinkGroup> list =  contactsLinkGroupDao.findByGroupIdInAndContactsId(groupIds, contactsId);
        for (ContactsLinkGroup clg:list) {
            map.remove(clg.getGroupId());
        }
        contactsLinkGroupDao.saveAll(map.values());
    }

    @Override
    @Transactional
    public void deleteByContactsGroupIdAndContactsIdIn(Long groupId, List<Long> contactsIds) {
        checkParam(groupId,contactsIds);
        contactsLinkGroupDao.deleteByContactsIdInAndGroupId(contactsIds, groupId);
    }

    private void checkParam(Long groupId, List<Long> contactsIds){
        Long customerId = Current.getUserDetails().getId();
        Long count = contactsService.countByIdInAndCustomerId(contactsIds, customerId);
        ServiceException.isTrue(count==contactsIds.size(),returnMsgUtil.msg("CONTACTS_NOT_EXISTS"));
        ContactsGroup contactsGroup = contactsGroupService.findById(groupId);
        ServiceException.notNull(contactsGroup, returnMsgUtil.msg("GROUP_INFO_ERROR"));
        ServiceException.isTrue(contactsGroup.getCustomerId().equals(customerId), returnMsgUtil.msg("GROUP_INFO_ERROR"));
    }
}
