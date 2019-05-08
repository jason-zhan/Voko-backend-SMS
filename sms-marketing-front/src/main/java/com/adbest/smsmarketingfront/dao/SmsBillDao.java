package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.SmsBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SmsBillDao extends JpaRepository<SmsBill, Long>, JpaSpecificationExecutor<SmsBill> {

}
