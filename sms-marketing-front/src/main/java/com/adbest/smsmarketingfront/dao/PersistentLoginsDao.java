package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.PersistentLogins;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersistentLoginsDao extends JpaRepository<PersistentLogins, Long> {
}
