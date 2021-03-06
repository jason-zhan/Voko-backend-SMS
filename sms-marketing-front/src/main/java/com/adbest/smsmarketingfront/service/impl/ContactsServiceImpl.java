package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.dao.ContactsDao;
import com.adbest.smsmarketingentity.ContactsSource;
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
import com.adbest.smsmarketingfront.util.*;
import com.google.common.collect.Lists;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
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

    @Autowired
    private RedisLockUtil redisLockUtil;

    @Autowired
    private ContactsService contactsService;

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
        ServiceException.isTrue(checkPhone(contactsForm.getPhone()), returnMsgUtil.msg("PHONE_INCORRECT_FORMAT"));
        Long customerId = Current.get().getId();
        Contacts contacts = contactsForm.getContacts();
        Contacts ct = contactsDao.findFirstByCustomerIdAndPhone(customerId, contactsForm.getPhone());
        if (ct==null){
            contacts.setIsDelete(false);
            contacts.setInLock(false);
            contacts.setCustomerId(customerId);
            contacts.setSource(ContactsSource.Manually_Added.getValue());
        }else {
            contacts = contactsForm.getContacts(ct);
            contacts.setIsDelete(false);
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
        Long customerId = Current.get().getId();
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
        Long customerId = Current.get().getId();
        Optional<Contacts> Optional = contactsDao.findById(Long.valueOf(id));
        ServiceException.isTrue(Optional.isPresent(), returnMsgUtil.msg("CONTACTS_NOT_EXISTS"));
        Contacts contacts = Optional.get();
        ServiceException.isTrue(contacts.getCustomerId()==customerId, returnMsgUtil.msg("CONTACTS_NOT_EXISTS"));
        contacts.setInLock(isLock);
        if (isLock){
            contacts.setInLockTime(new Timestamp(System.currentTimeMillis()));
        }else {
            contacts.setInLockTime(null);
        }
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
        Long customerId = Current.get().getId();
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
        Long customerId = Current.get().getId();
        BooleanBuilder builder = new BooleanBuilder();
        QContacts qContacts = QContacts.contacts;
        QContactsLinkGroup qContactsLinkGroup = QContactsLinkGroup.contactsLinkGroup;
        if(!StringUtils.isEmpty(selectContactsForm.getKeyWord())){
                builder.and(qContacts.phone.containsIgnoreCase(selectContactsForm.getKeyWord()).or(qContacts.firstName.containsIgnoreCase
                        (selectContactsForm.getKeyWord())));
        }
        if(!StringUtils.isEmpty(selectContactsForm.getSource())){
            builder.and(qContacts.source.eq(Integer.valueOf(selectContactsForm.getSource())));
        }
        if(selectContactsForm.getInLock()!=null){
            builder.and(qContacts.inLock.eq(selectContactsForm.getInLock()));
        }
        builder.and(qContacts.customerId.eq(customerId));
        builder.and(qContacts.isDelete.isFalse());
        JPAQuery<Contacts> jpaQuery = null;
        if (!StringUtils.isEmpty(selectContactsForm.getGroupId())){
            builder.and(qContactsLinkGroup.groupId.eq(Long.valueOf(selectContactsForm.getGroupId())));
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
//        Map<Long, Contacts> contactsMap = page.stream().collect(Collectors.toMap(Contacts::getId, contacts -> contacts));
        List<ContactsVo> contactsVos = page.getContent().stream().map(s -> new ContactsVo(s)).collect(Collectors.toList());
        Map<Long, ContactsVo> contactsMap = contactsVos.stream().collect(Collectors.toMap(ContactsVo::getId, contacts -> contacts));
        if (page.getContent().size()>0){
            List<Long> ids = page.getContent().stream().map(s -> s.getId()).collect(Collectors.toList());
            List<Object> list = contactsGroupService.findByContentIn(ids);
            ContactsGroup contactsGroup = null;
            for (Object obj : list) {
                Object[] objects = (Object[]) obj;
                if (objects[2]==null){continue;}
                ContactsVo contacts = contactsMap.get(Long.valueOf(objects[0].toString()));
                contactsGroup = new ContactsGroup();
                contactsGroup.setTitle(objects[1].toString());
                contactsGroup.setId(Long.valueOf(objects[2].toString()));
                if (contacts.getGroups()==null){
                    contacts.setGroups(new ArrayList<>());
                }
                contacts.getGroups().add(contactsGroup);
            }
        }
        return new PageDataVo(page, contactsVos);
    }

    @Override
    public boolean process(ContactsProcessForm contactsProcessForm) {
        ServiceException.isTrue(!StringUtils.isEmpty(contactsProcessForm.getGroupId()) && !StringUtils.isEmpty(contactsProcessForm.getTempSign()),
                returnMsgUtil.msg("MISSING_PARAMETER"));
        Long customerId = Current.get().getId();
        List<ContactsTemp> list = contactsTempService.findByTempSign(contactsProcessForm.getTempSign());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                contactsService.saveData(list, customerId, contactsProcessForm.getGroupId());
            }
        };
        runnable.run();;
        return true;
    }

    @Override
    public List<ContactsVo> upload(MultipartFile file) {
        ServiceException.notNull(file,returnMsgUtil.msg("FILE_NOT_EMPTY"));
        try {
            String type = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1, file.getOriginalFilename().length());
            ServiceException.isTrue("xls".equals(type)||"xlsx".equals(type),returnMsgUtil.msg("FILE_INCORRECT_FORMAT"));
            InputStream in = file.getInputStream();
            List<List<Object>> listob = new ArrayList<List<Object>>();
            listob = new ImportExcelUtil().getBankListByExcel(in, file.getOriginalFilename());
            List<ContactsVo> list = Lists.newArrayList();
            ContactsVo vo = null;
            ServiceException.isTrue(listob.size()<=2000,returnMsgUtil.msg("TOO_MANY_IMPORTS"));
            if (listob.size()>1)
                for (int i = 1;i<listob.size();i++){
                    List<Object> objects = listob.get(i);
                    if (objects.size()==0)continue;
                    vo = new ContactsVo();
                    vo.setPhone(objects.get(0)==null?null:objects.get(0).toString());
                    vo.setFirstName(objects.get(1)==null?null:objects.get(1).toString());
                    vo.setLastName(objects.get(2)==null?null:objects.get(2).toString());
                    vo.setEmail(objects.get(3)==null?null:objects.get(3).toString());
                    vo.setNotes(objects.get(4)==null?null:objects.get(4).toString());
                    list.add(vo);
                }
            return list;
        }catch(Exception e){
            log.error("Excel Import error",e);
            throw new ServiceException(returnMsgUtil.msg("T500"));
        }
    }

    @Override
    @Transactional
    public void saveAll(List<Contacts> contactsList) {
        contactsDao.saveAll(contactsList);
    }

    @Override
    @Transactional
    public Contacts save(Contacts contacts) {
        return contactsDao.save(contacts);
    }

    @Override
    public List<Contacts> findByPhoneAndCustomerId(String from, Long customerId) {
        return contactsDao.findByPhoneAndCustomerId(from, customerId);
    }

    @Transactional
    public void saveData(List<ContactsTemp> list,Long customerId,String groupId){
        if (list.size()<=0)return;
        List<Contacts> contacts = contactsDao.findByCustomerId(customerId);
        Map<String,Contacts> map = contacts.stream().collect(Collectors.toMap(Contacts::getPhone,c -> c, (contacts1, newContacts) -> contacts1));
        contacts = Lists.newArrayList();
        Contacts c = null;
        for (ContactsTemp contactsTemp:list) {
            boolean lock = redisLockUtil.lock(customerId + contactsTemp.getPhone(), 60 * 5);
            if (!lock || !checkPhone(contactsTemp.getPhone()))continue;
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

    private boolean checkPhone(String phone) {
        Pattern pattern = Pattern.compile("^(\\d{10})$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }
}
