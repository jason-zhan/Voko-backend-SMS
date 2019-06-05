package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.SystemMsgTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SystemMsgTemplateDao extends JpaRepository<SystemMsgTemplate, Long>, JpaSpecificationExecutor<SystemMsgTemplate> {

    SystemMsgTemplate findByIdAndDisableIsFalse(Long id);
}
