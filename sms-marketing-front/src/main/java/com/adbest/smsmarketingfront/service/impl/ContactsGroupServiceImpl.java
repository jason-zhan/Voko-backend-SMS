package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.ContactsGroup;
import com.adbest.smsmarketingentity.ContactsLinkGroup;
import com.adbest.smsmarketingfront.dao.ContactsGroupDao;
import com.adbest.smsmarketingfront.entity.form.ContactsGroupForm;
import com.adbest.smsmarketingfront.entity.vo.PageDataVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.ContactsGroupService;
import com.adbest.smsmarketingfront.service.ContactsLinkGroupService;
import com.adbest.smsmarketingfront.service.ContactsService;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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

    @Autowired
    private ReturnMsgUtil returnMsgUtil;

    @Override
    @Transactional
    public ContactsGroup save(ContactsGroupForm contactsGroup) {
        ServiceException.notNull(contactsGroup.getTitle(), returnMsgUtil.msg("GROUP_NAME_NOT_EMPTY"));
        Long count = countByCustomerIdAndTitle(contactsGroup.getCustomerId(), contactsGroup.getTitle());
        ServiceException.isTrue(count<=0, returnMsgUtil.msg("GROUP_NAME_EXISTS"));
        ContactsGroup co = contactsGroupDao.save(contactsGroup.getContactsGroup());
        List<String> groupIds = contactsGroup.getGroupIds();
        if (groupIds!=null && groupIds.size()>0){
            List<Long> ids = groupIds.stream().map(s -> Long.valueOf(s)).collect(Collectors.toList());
            List<ContactsLinkGroup> contactsLinkGroups = contactsLinkGroupService.findByGroupIdIn(ids);
            if (contactsLinkGroups.size()>0){
                List<Long> contactsIds = contactsLinkGroups.stream().map(s -> s.getContactsId()).distinct().collect(Collectors.toList());
                contactsLinkGroupService.createContactsLinkGroup(contactsIds, co.getId());
//                List<ContactsLinkGroup> list = new ArrayList<>();
//                ContactsLinkGroup clg = null;
//                for (Long id:contactsIds) {
//                    clg = new ContactsLinkGroup(id, co.getId());
//                    list.add(clg);
//                }
//                contactsLinkGroupService.saveAll(list);
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
    public PageDataVo findAll(PageBase page) {
        Pageable pageable = PageRequest.of(page.getPage(), page.getSize());
        Page<ContactsGroup> data = contactsGroupDao.findByCustomerId(Current.get().getId(), pageable);
        return new PageDataVo(data);
    }

    @Override
    @Transactional
    public ContactsGroup update(ContactsGroupForm contactsGroupForm) {
        ServiceException.notNull(contactsGroupForm.getTitle(), returnMsgUtil.msg("GROUP_NAME_NOT_EMPTY"));
        Optional<ContactsGroup> optionalContactsGroup = contactsGroupDao.findById(contactsGroupForm.getId());
        ServiceException.isTrue(optionalContactsGroup.isPresent(),returnMsgUtil.msg("GROUP_INFO_NOT_EXISTS"));
        ServiceException.isTrue(optionalContactsGroup.get().getCustomerId()== Current.get().getId(),returnMsgUtil.msg("GROUP_INFO_NOT_EXISTS"));
        ContactsGroup contactsGroup = optionalContactsGroup.get();
        if (!contactsGroup.getTitle().equals(contactsGroup.getTitle())){
            Long count = countByCustomerIdAndTitle(Current.get().getId(), contactsGroupForm.getTitle());
            ServiceException.isTrue(count<=0, returnMsgUtil.msg("GROUP_NAME_EXISTS"));
        }
        contactsGroup.setTitle(contactsGroupForm.getTitle());
        contactsGroup.setDescription(contactsGroupForm.getDescription());
        return contactsGroupDao.save(contactsGroup);
    }

    @Override
    @Transactional
    public Integer delete(List<String> groupIds) {
        ServiceException.isTrue(groupIds!=null, returnMsgUtil.msg("GROUP_INFO_NOT_EXISTS"));
        Long customerId = Current.get().getId();
        List<Long> ids = groupIds.stream().map(s -> Long.valueOf(s)).collect(Collectors.toList());
        Long count = contactsGroupDao.countByIdInAndCustomerId(ids, customerId);
        ServiceException.isTrue(count==ids.size(), returnMsgUtil.msg("GROUP_INFO_ERROR"));
        Integer row = contactsGroupDao.deleteByIdIn(ids);
        contactsLinkGroupService.deleteByContactsGroupIdIn(ids);
        return row;
    }

    @Override
    public PageDataVo contacts(String id, PageBase pageBase) {
        return contactsService.findByContactsGroupId(id, pageBase);
    }

    @Override
    public Long countByIdInAndCustomerId(List<Long> gids, Long customerId) {
        return contactsGroupDao.countByIdInAndCustomerId(gids, customerId);
    }

    @Override
    public ContactsGroup findById(Long groupId) {
        Optional<ContactsGroup> optional = contactsGroupDao.findById(groupId);
        if (optional.isPresent())
            return optional.get();
        return null;
    }

    @Override
    public List<ContactsGroup> findAll() {
        Long customerId = Current.get().getId();
        return contactsGroupDao.findByCustomerId(customerId);
    }

    @Override
    public List<Object> findByContentIn(List<Long> ids) {
        return contactsGroupDao.findByContentIn(ids);
    }

}
