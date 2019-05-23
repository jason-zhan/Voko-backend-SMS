package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.ContactsGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface ContactsGroupDao extends JpaRepository<ContactsGroup, Long> {
    Long countByCustomerIdAndTitle(Long customerId, String title);

    @Modifying
    @Transactional
    @Query(value = "delete from ContactsGroup c where c.id in :ids")
    Integer deleteByIdIn(List<Long> ids);

    Long countByIdInAndCustomerId(List<Long> ids, Long customerId);

    Long deleteByIdInAndCustomerId(List<Long> ids, Long id);

    Page<ContactsGroup> findByCustomerId(Long customerId, Pageable pageable);

    @Query(value = "SELECT a.contactsId,b.title,b.id FROM contacts_link_group a LEFT JOIN contacts_group b ON a.groupId = b.id where a.contactsId in :ids"
            ,nativeQuery = true)
    List<Object> findByContentIn(@Param("ids") List<Long> ids);

    List<ContactsGroup> findByCustomerId(Long customerId);
 
    ContactsGroup findByIdAndCustomerId(Long id, Long customerId);
}
