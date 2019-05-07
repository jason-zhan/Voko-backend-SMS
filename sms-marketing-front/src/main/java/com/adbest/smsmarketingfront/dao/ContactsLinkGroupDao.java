package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.ContactsLinkGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ContactsLinkGroupDao extends JpaRepository<ContactsLinkGroup, Long> {

    List<ContactsLinkGroup> findByGroupIdIn(List<Long> groupIds);

    @Modifying
    @Transactional
    @Query(value = "delete from ContactsLinkGroup c where c.groupId in :ids")
    Integer deleteByContactsGroupIdIn(List<Long> ids);

    @Modifying
    @Transactional
    @Query(value = "delete from ContactsLinkGroup c where c.contactsId in :ids")
    Integer deleteByContactsIdIn(List<Long> ids);
}
