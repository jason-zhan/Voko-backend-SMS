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

    @Query(value = "SELECT a.contactsId,b.title,b.id FROM ContactsLinkGroup a LEFT JOIN ContactsGroup b ON a.groupId = b.id where a.contactsId in :ids")
    List<Object> findByContentIn(@Param("ids") List<Long> ids);

    List<ContactsGroup> findByCustomerId(Long customerId);
 
    ContactsGroup findByIdAndCustomerId(Long id, Long customerId);

   @Query(value = "SELECT a.id,a.title,IFNULL(b.num,0) FROM " +
           "(SELECT a.id,a.title FROM contacts_group a WHERE a.customer_id = :customerId) a LEFT JOIN  " +
           "(SELECT group_id,count(DISTINCT contacts_id) num FROM contacts_link_group c WHERE group_id  " +
           "in (SELECT id FROM contacts_group WHERE customer_id = :customerId) GROUP BY group_id) as b ON a.id = b.group_id order by a.id;",nativeQuery = true)
    List<?> selectByCustomerId(Long customerId);
}
