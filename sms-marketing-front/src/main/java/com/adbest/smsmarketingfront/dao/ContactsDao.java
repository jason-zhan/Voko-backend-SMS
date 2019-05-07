package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.Contacts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ContactsDao extends JpaRepository<Contacts, Long> {
    @Query("select c from Contacts c left join ContactsLinkGroup clg on c.id = clg.contactsId where clg.groupId = :contactsGroupId and c.isDelete = false ")
    Page<Contacts> findByContactsGroupId(Long contactsGroupId, Pageable pageable);
}
