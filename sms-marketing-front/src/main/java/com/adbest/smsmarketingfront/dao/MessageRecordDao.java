package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MessageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MessageRecordDao extends JpaRepository<MessageRecord, Integer>, JpaSpecificationExecutor<MessageRecord> {
}
