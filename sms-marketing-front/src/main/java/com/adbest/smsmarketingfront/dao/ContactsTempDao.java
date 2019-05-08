package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.ContactsTemp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactsTempDao extends JpaRepository<ContactsTemp, Long> {
    List<ContactsTemp> findByTempSign(String tempSign);
}
