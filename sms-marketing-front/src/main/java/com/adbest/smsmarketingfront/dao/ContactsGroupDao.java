package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.ContactsGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

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

    Page<ContactsGroup> findByCustomerIdAndIsDelete(Long customerId, boolean disable, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "update ContactsGroup c set c.isDelete = true where c.customerId = :customerId and c.id in :ids")
    Integer updateIsDisableByCustomerAndIdIn(Long customerId, List<Long> ids);
}
