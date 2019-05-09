package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.Contacts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ContactsDao extends JpaRepository<Contacts, Long>, JpaSpecificationExecutor<Contacts> {
    @Query("select c from Contacts c left join ContactsLinkGroup clg on c.id = clg.contactsId where clg.groupId = :contactsGroupId and c.isDelete = false ")
    Page<Contacts> findByContactsGroupId(Long contactsGroupId, Pageable pageable);

    Contacts findFirstByPhoneAndCustomerId(String phone, Long customerId);

    @Modifying
    @Transactional
    @Query(value = "update Contacts c set c.isDelete = true where c.customerId = :customerId and c.id in :ids")
    Integer updateIsDisableByCustomerIdAndIdIn(Long customerId, List<Long> ids);

    Long countByIdInAndCustomerId(List<Long> contactsIds, Long customerId);

    List<Contacts> findByCustomerId(Long customerId);
}
