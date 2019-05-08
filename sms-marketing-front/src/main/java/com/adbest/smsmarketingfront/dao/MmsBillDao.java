package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MmsBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MmsBillDao extends JpaRepository<MmsBill, Long>, JpaSpecificationExecutor<MmsBill> {

}
