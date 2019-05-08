package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MbNumberLib;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MbNumberLibDao extends JpaRepository<MbNumberLib, Long>, JpaSpecificationExecutor<MbNumberLib> {

}
