package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.Contacts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface ContactsDao extends JpaRepository<Contacts, Long>, JpaSpecificationExecutor<Contacts> {
    @Query("select c from Contacts c left join ContactsLinkGroup clg on c.id = clg.contactsId where clg.groupId = :contactsGroupId and c.isDelete = false ")
    Page<Contacts> findByContactsGroupId(@Param("contactsGroupId") Long contactsGroupId, Pageable pageable);
    
    @Query("select c from Contacts c left join ContactsLinkGroup link on c.id = link.contactsId where link.groupId = ?1 and c.inLock = false and c.isDelete = false ")
    Page<Contacts> findUsableByGroupId(Long groupId, Pageable pageable);
    
    Contacts findFirstByCustomerIdAndPhone(Long customerId, String phone);
    
    @Modifying
    @Transactional
    @Query(value = "update Contacts c set c.isDelete = true where c.customerId = :customerId and c.id in :ids")
    Integer updateIsDisableByCustomerIdAndIdIn(Long customerId, List<Long> ids);
    
    Long countByIdInAndCustomerId(List<Long> contactsIds, Long customerId);
    
    List<Contacts> findByCustomerId(Long customerId);
    
    Contacts findByIdAndCustomerIdAndIsDeleteIsFalse(Long id, Long customerId);
    
    List<Contacts> findByPhoneAndCustomerId(String from, Long customerId);
    
    @Query("select c from  Contacts c inner join ContactsLinkGroup link on c.id = link.contactsId " +
            "where c.customerId = ?1 and link.groupId = ?2 and c.inLock = false and c.isDelete = false ")
    Page<Contacts> findUsableByCustomerIdAndGroupId(Long customerId, Long groupId, Pageable pageable);
}
