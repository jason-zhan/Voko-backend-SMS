package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.dao.ContactsDao;
import com.adbest.smsmarketingfront.entity.enums.ContactsSource;
import com.adbest.smsmarketingfront.entity.form.AddContactsToGroupsForm;
import com.adbest.smsmarketingfront.entity.form.ContactsForm;
import com.adbest.smsmarketingfront.entity.form.ContactsProcessForm;
import com.adbest.smsmarketingfront.entity.form.SelectContactsForm;
import com.adbest.smsmarketingfront.entity.vo.ContactsVo;
import com.adbest.smsmarketingfront.entity.vo.PageDataVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.ContactsGroupService;
import com.adbest.smsmarketingfront.service.ContactsLinkGroupService;
import com.adbest.smsmarketingfront.service.ContactsService;
import com.adbest.smsmarketingfront.service.ContactsTempService;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import com.google.common.collect.Lists;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContactsServiceImpl implements ContactsService {

    @Autowired
    private ContactsDao contactsDao;

    @Autowired
    private ContactsLinkGroupService contactsLinkGroupService;

    @Autowired
    private ContactsGroupService contactsGroupService;

    @Autowired
    private ReturnMsgUtil returnMsgUtil;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    private ContactsTempService contactsTempService;

    @Override
    public PageDataVo findByContactsGroupId(String contactsGroupId, PageBase pageBase) {
        Pageable pageable = PageRequest.of(pageBase.getPage(), pageBase.getSize());
        Page<Contacts> page = contactsDao.findByContactsGroupId(Long.valueOf(contactsGroupId), pageable);
        return new PageDataVo(page);
    }

    @Override
    @Transactional
    public ContactsVo save(ContactsForm contactsForm) {
        ServiceException.notNull(contactsForm.getPhone(), returnMsgUtil.msg("PHONE_NOT_EMPTY"));
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
            contactsLinkGroupService.deleteByContactsIdIn(Arrays.asList(contacts.getId()));
            contactsLinkGroupService.createContactsLinkGroup(contacts.getId(), gids);
        }
        return new ContactsVo(contacts);
    }

    @Override
    @Transactional
    public Integer delete(String ids) {
        ServiceException.notNull(ids, returnMsgUtil.msg("CONTACTS_NOT_EMPTY"));
        Long customerId = Current.getUserDetails().getId();
        List<String> id = Arrays.asList(ids.split(","));
        List<Long> idList = id.stream().map(s -> Long.valueOf(s)).collect(Collectors.toList());
        Integer row = contactsDao.updateIsDisableByCustomerIdAndIdIn(customerId, idList);
        ServiceException.isTrue(row==idList.size(),returnMsgUtil.msg("CONTACTS_NOT_EXISTS"));
        contactsLinkGroupService.deleteByContactsIdIn(idList);
        return row;
    }

    @Override
    @Transactional
    public Boolean updateLock(String id, Boolean isLock) {
        ServiceException.notNull(id, returnMsgUtil.msg("CONTACTS_NOT_EMPTY"));
        ServiceException.notNull(isLock, returnMsgUtil.msg("INLOCK_NOT_EMPTY"));
        Long customerId = Current.getUserDetails().getId();
        Optional<Contacts> Optional = contactsDao.findById(Long.valueOf(id));
        ServiceException.isTrue(Optional.isPresent(), returnMsgUtil.msg("CONTACTS_NOT_EXISTS"));
        Contacts contacts = Optional.get();
        ServiceException.isTrue(contacts.getCustomerId()==customerId, returnMsgUtil.msg("CONTACTS_NOT_EXISTS"));
        contacts.setIsDelete(isLock);
        contacts.setInLockTime(new Timestamp(System.currentTimeMillis()));
        contactsDao.save(contacts);
        return true;
    }

    @Override
    @Transactional
    public Boolean addContactsToGroups(AddContactsToGroupsForm addContactsToGroupsForm) {
        ServiceException.notNull(addContactsToGroupsForm.getContactsIds(), returnMsgUtil.msg("CONTACTS_NOT_EMPTY"));
        ServiceException.notNull(addContactsToGroupsForm.getGroupId(), returnMsgUtil.msg("GROUP_NOT_EMPTY"));
        contactsLinkGroupService.createContactsLinkGroup(addContactsToGroupsForm.getContactsIds(),addContactsToGroupsForm.getGroupId());
        return true;
    }

    @Override
    @Transactional
    public Boolean outContactsToGroups(AddContactsToGroupsForm addContactsToGroupsForm) {
        ServiceException.notNull(addContactsToGroupsForm.getContactsIds(), returnMsgUtil.msg("CONTACTS_NOT_EMPTY"));
        ServiceException.notNull(addContactsToGroupsForm.getGroupId(), returnMsgUtil.msg("GROUP_NOT_EMPTY"));
        contactsLinkGroupService.deleteByContactsGroupIdAndContactsIdIn(addContactsToGroupsForm.getGroupId(), addContactsToGroupsForm.getContactsIds());
        return true;
    }

    @Override
    public Long countByIdInAndCustomerId(List<Long> contactsIds, Long customerId) {
        return contactsDao.countByIdInAndCustomerId(contactsIds, customerId);
    }

    @Override
    public PageDataVo select(SelectContactsForm selectContactsForm) {
        Long customerId = Current.getUserDetails().getId();
        Specification querySpecifi = new Specification<Contacts>() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if(selectContactsForm.getKeyWord()!=null){
                    predicates.add(criteriaBuilder.or(criteriaBuilder.like(root.get("phone"), "%"+selectContactsForm.getKeyWord()+"%"),criteriaBuilder.like(root.get("firstName"), "%"+selectContactsForm.getKeyWord()+"%")));
                }
                if(selectContactsForm.getSource()!=null){
                    predicates.add(criteriaBuilder.equal(root.get("source"), selectContactsForm.getSource()));
                }
                if(selectContactsForm.getInLock()!=null){
                    predicates.add(criteriaBuilder.equal(root.get("inLock"), selectContactsForm.getInLock()));
                }
                predicates.add(criteriaBuilder.equal(root.get("customerId"), customerId));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        Page<Contacts> page = contactsDao.findAll(querySpecifi, PageRequest.of(selectContactsForm.getPage(), selectContactsForm.getSize()));
        return new PageDataVo(page);
    }

    public PageDataVo selectAll(SelectContactsForm selectContactsForm) {
        Long customerId = Current.getUserDetails().getId();
        BooleanBuilder builder = new BooleanBuilder();
        QContacts qContacts = QContacts.contacts;
        QContactsLinkGroup qContactsLinkGroup = QContactsLinkGroup.contactsLinkGroup;
        if(selectContactsForm.getKeyWord()!=null){
                builder.and(qContacts.phone.containsIgnoreCase(selectContactsForm.getKeyWord()).or(qContacts.firstName.containsIgnoreCase
                        (selectContactsForm.getKeyWord())));
        }
        if(selectContactsForm.getSource()!=null){
            builder.and(qContacts.source.eq(Integer.valueOf(selectContactsForm.getSource())));
        }
        if(selectContactsForm.getInLock()!=null){
            builder.and(qContacts.inLock.eq(selectContactsForm.getInLock()));
        }
        builder.and(qContacts.customerId.eq(customerId));
        JPAQuery<Contacts> jpaQuery = null;
        if (selectContactsForm.getGroupId()!=null){
            builder.and(qContactsLinkGroup.id.eq(Long.valueOf(selectContactsForm.getGroupId())));
            jpaQuery = jpaQueryFactory.select(qContacts)
                    .from(qContacts).rightJoin(qContactsLinkGroup).on(qContacts.id.eq(qContactsLinkGroup.contactsId));
        }else {
            jpaQuery = jpaQueryFactory.select(qContacts).from(qContacts);
        }
        QueryResults<Contacts> queryResults = jpaQuery.where(builder)
                .offset(selectContactsForm.getPage() * selectContactsForm.getSize())
                .limit(selectContactsForm.getSize())
                .fetchResults();;
        Page<Contacts> page = PageBase.toPageEntity(queryResults, selectContactsForm);
        return new PageDataVo(page);
    }

    @Override
    public boolean process(ContactsProcessForm contactsProcessForm) {
        ServiceException.isTrue(contactsProcessForm.getGroupId()!=null && contactsProcessForm.getTempSign()!=null,returnMsgUtil.msg("MISSING_PARAMETER"));
        Long customerId = Current.getUserDetails().getId();
        List<ContactsTemp> list = contactsTempService.findByTempSign(contactsProcessForm.getTempSign());
        new Runnable() {
            @Override
            public void run() {
                saveData(list, customerId,contactsProcessForm.getGroupId());
            }
        };
        return true;
    }

    @Transactional
    public void saveData(List<ContactsTemp> list,Long customerId,String groupId){
        if (list.size()<=0)return;
        List<Contacts> contacts = contactsDao.findByCustomerId(customerId);
        Map<String,Contacts> map = contacts.stream().collect(Collectors.toMap(Contacts::getPhone,c -> c));
        contacts = Lists.newArrayList();
        Contacts c = null;
        for (ContactsTemp contactsTemp:list) {
            c = map.get(contactsTemp.getPhone());
            if (c==null){
                c = new Contacts();
                c.setPhone(contactsTemp.getPhone());
                c.setSource(ContactsSource.Upload.getValue());
                c.setCustomerId(customerId);
                c.setInLock(false);
                c.setIsDelete(false);
            }
            c.setFirstName(contactsTemp.getFirstName());
            c.setLastName(contactsTemp.getLastName());
            c.setEmail(contactsTemp.getEmail());
            c.setNotes(contactsTemp.getNotes());
            contacts.add(c);
        }
        contactsDao.saveAll(contacts);
        contactsLinkGroupService.createContactsLinkGroup(contacts.stream().map(s->s.getId()).collect(Collectors.toList()), Long.valueOf(groupId));
    }
}
