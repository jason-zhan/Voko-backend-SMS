package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.ContactsLinkGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactsLinkGroupDao extends JpaRepository<ContactsLinkGroup, Long> {
    List<ContactsLinkGroup> findByGroupIdIn(List<Long> groupIds);
}
