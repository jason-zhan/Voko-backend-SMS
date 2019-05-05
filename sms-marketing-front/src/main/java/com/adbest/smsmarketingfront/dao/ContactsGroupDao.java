package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.ContactsGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactsGroupDao extends JpaRepository<ContactsGroup, Long> {
    Long countByCustomerIdAndTitle(Long customerId, String title);

    Long deleteByIdIn(List<Long> ids);

    Long countByIdInAndCustomerId(List<Long> ids, Long customerId);

    Page<ContactsGroup> findByCustomerId(Long id, Pageable pageable);

    Long deleteByIdInAndCustomerId(List<Long> ids, Long id);
}
